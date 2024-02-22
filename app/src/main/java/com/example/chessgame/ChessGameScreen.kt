package com.example.chessgame


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chessgame.ui.screens.AboutScreen
import com.example.chessgame.ui.screens.MenuScreen
import com.example.chessgame.ui.screens.PlayScreen
import com.example.chessgame.ui.screens.PuzzlesScreen


enum class ChessGameScreen(){
    Menu,
    Play,
    Puzzles,
    About
}

@Composable
fun ChessGameApp(
    navController: NavHostController = rememberNavController()
){
    NavHost(
        navController = navController,
        startDestination = ChessGameScreen.Menu.name,
    ){
        composable(route = ChessGameScreen.Menu.name){
            MenuScreen(
                onPlayButtonClicked = {navController.navigate(ChessGameScreen.Play.name)},
                onPuzzlesButtonClicked = {navController.navigate(ChessGameScreen.Puzzles.name)},
                onAboutButtonClicked = {navController.navigate(ChessGameScreen.About.name)}
            )
        }
        composable(route = ChessGameScreen.Play.name){
            PlayScreen()
        }
        composable(route = ChessGameScreen.Puzzles.name){
            PuzzlesScreen()
        }
        composable(route = ChessGameScreen.About.name){
            AboutScreen()
        }
    }
}