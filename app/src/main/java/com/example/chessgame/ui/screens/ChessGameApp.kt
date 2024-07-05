package com.example.chessgame.ui.screens


import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chessgame.interfaces.BluetoothManager
import com.example.chessgame.ui.ChessGameViewModel

enum class ChessGameScreen(){
    Menu,
    Play,
    Puzzles,
    Record,
    Watch,
    Practice,
    About
}

// Basically the NavHost element which allows the user to switch between all the screens of the app

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChessGameApp(
    context: Context,
    chessGameViewModel: ChessGameViewModel,
    bluetoothManager: BluetoothManager,
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
                onRecordButtonClicked = {navController.navigate(ChessGameScreen.Record.name)},
                onWatchButtonClicked = {navController.navigate(ChessGameScreen.Watch.name)},
                onAboutButtonClicked = {navController.navigate(ChessGameScreen.About.name)},
                bluetoothManager = bluetoothManager,
            )
        }
        composable(route = ChessGameScreen.Play.name + "/{difficulty}") { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Easy" // Default to "Easy" if not specified
            PlayScreen(chessGameViewModel, true, difficulty)
        }
        composable(route = ChessGameScreen.Puzzles.name){
            PuzzlesScreen(chessGameViewModel)
        }
        composable(route = ChessGameScreen.Record.name){
            RecordMatchScreen(context, chessGameViewModel)
        }
        composable(route = ChessGameScreen.Watch.name){
            WatchRecordingScreen(context, chessGameViewModel)
        }
        composable(route = ChessGameScreen.Practice.name){
            PlayScreen(chessGameViewModel, false, "Hard")
        }
        composable(route = ChessGameScreen.About.name){
            AboutScreen()
        }
    }
}