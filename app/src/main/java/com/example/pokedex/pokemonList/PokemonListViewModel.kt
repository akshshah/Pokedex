package com.example.pokedex.pokemonList

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.pokedex.PokemonRepository
import com.example.pokedex.data.models.PokedexListItem
import com.example.pokedex.util.Constants
import com.example.pokedex.util.Constants.PAGE_SIZE
import com.example.pokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(private val repository: PokemonRepository) : ViewModel() {

    private var currPage = 0

    var pokemonList = mutableStateOf<List<PokedexListItem>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    private var cachedPokemonList = listOf<PokedexListItem>()
    private var isSearchStarting = true
    var isSearching = mutableStateOf(false)

    init {
        loadPokemonPagination()
    }

    fun searchPokemonList(query: String){
        val listToSearch = if(isSearchStarting){
            pokemonList.value
        }else{
            cachedPokemonList
        }
        viewModelScope.launch(Dispatchers.Default) {
            if(query.isEmpty()){
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }
            val results = listToSearch.filter {
                it.pokemonName.contains(query.trim(), ignoreCase = true) ||
                        it.number.toString() == query.trim()
            }

            if(isSearchStarting){
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }

            pokemonList.value = results
            isSearching.value = true
        }
    }

    fun loadPokemonPagination(){
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.getPokemonList(PAGE_SIZE, currPage.times(PAGE_SIZE))
            when(result){
                is Resource.Success -> {
                    endReached.value = currPage.times(PAGE_SIZE) >= result.data!!.count
                    val pokedexItems = result.data.results.mapIndexed { index, item ->
                        val number = if(item.url.endsWith("/")){
                            item.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            item.url.takeLastWhile { it.isDigit() }
                        }

                        val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"

                        PokedexListItem(item.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.ROOT
                            ) else it.toString()
                        }, imageUrl = url, number = number.toInt())
                    }
                    currPage++

                    loadError.value = ""
                    isLoading.value = false
                    pokemonList.value += pokedexItems
                }
                is Resource.Error -> {
                    loadError.value = result.message!!
                    isLoading.value = false
                }
                else -> {
                    return@launch
                }
            }
        }
    }

    fun calculateDominantColor(drawable: Drawable, onFinish: (Color) -> Unit ){
        val bitmap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bitmap).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
            }
        }
    }

}