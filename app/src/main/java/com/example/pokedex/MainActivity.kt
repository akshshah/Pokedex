package com.example.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.toLowerCase
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.pokemonList.PokemonListScreen
import com.example.pokedex.pokemondetail.PokemonDetailsScreen
import com.example.pokedex.ui.theme.PokedexTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokedexTheme {
                Navigation()
            }
        }
    }
}

@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.PokemonListScreen.route){
        composable(Screen.PokemonListScreen.route){
            PokemonListScreen(navController = navController)
        }
        composable(Screen.PokemonDetailsScreen.route + "/{dominantColor}/{pokemonName}", arguments = listOf(
            navArgument("dominantColor"){
                type = NavType.IntType
            },
            navArgument("pokemonName"){
                type = NavType.StringType
            }
        )){
            val dominantColor = remember {
                val color =  it.arguments?.getInt("dominantColor")
                color?.let { Color(it) } ?: Color.White
            }
            val pokemonName = remember {
                it.arguments?.getString("pokemonName")
            }

            PokemonDetailsScreen(dominantColor = dominantColor, pokemonName = pokemonName?.lowercase(
                Locale.ROOT
            ) ?: "", navController = navController)

        }
    }
}

