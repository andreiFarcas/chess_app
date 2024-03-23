package com.example.chessgame.ui.screens


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chessgame.ui.ChessGameViewModel


enum class ChessGameScreen(){
    Menu,
    Play,
    Puzzles,
    Practice,
    About
}

@Composable
fun ChessGameApp(
    chessGameViewModel: ChessGameViewModel,
    navController: NavHostController = rememberNavController(),
){
    NavHost(
        navController = navController,
        startDestination = ChessGameScreen.Menu.name,
    ){
        composable(route = ChessGameScreen.Menu.name){
            MenuScreen(
                navController = navController,
                //onPlayButtonClicked = {navController.navigate(ChessGameScreen.Play.name)},
                onPracticeButtonClicked = {navController.navigate(ChessGameScreen.Practice.name)},
                onPuzzlesButtonClicked = {navController.navigate(ChessGameScreen.Puzzles.name)},
                onAboutButtonClicked = {navController.navigate(ChessGameScreen.About.name)}
            )
        }
        composable(route = ChessGameScreen.Play.name + "/{difficulty}") { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Easy" // Default to "Easy" if not specified
            PlayScreen(chessGameViewModel, true, difficulty)
        }
        composable(route = ChessGameScreen.Puzzles.name){
            PuzzlesScreen()
        }
        composable(route = ChessGameScreen.Practice.name){
            PlayScreen(chessGameViewModel, false, "Hard")
        }
        composable(route = ChessGameScreen.About.name){
            AboutScreen()
        }
    }
}