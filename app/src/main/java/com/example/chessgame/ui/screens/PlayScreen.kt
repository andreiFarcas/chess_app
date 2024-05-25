package com.example.chessgame.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.sp
import com.example.chessgame.R
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.components.ChessBoardUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*

    Function that displays a "Play Screen" used both in play vs Stockfish or solo practice

 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PlayScreen(
    chessGameViewModel: ChessGameViewModel,
    playVsStockfish: Boolean,
    difficultyLevelStockfish: String,
){
    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()

    // Sets if we play vs stockfish or not (depends on user choice from  menu)
    chessGameViewModel.setPlayVsStockfish(playVsStockfish)
    chessGameViewModel.setDifficultyLevelStockfish(difficultyLevelStockfish)

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            chessGameViewModel.moveFromChessboard()
        }
    }


    Column {
        Text(
            text = "Here you will be able to play chess",
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "\nMove counter: ${boardState.moveCounter}"
        )
        Text(
            text = "Stockfish level: ${boardState.difficultyStockfish}"
        )
        ChessBoardUi(
            chessGameViewModel = chessGameViewModel,
            piecesState = boardState.piecesState,
            possibleMoves = boardState.possibleMoves,
            clickedSquare = boardState.clickedSquare,
            bKingInCheck = boardState.bKingInCheck
        )
        Button(onClick = {chessGameViewModel.resetBoard() }) {
            Text(text = "Reset Board")
        }
        Text(
            text = "\n FEN code for position:\n ${chessGameViewModel.testFenInterface()}\n",
            fontSize = 10.sp
        )

        if(!chessGameViewModel.checkPlayVsStockfish()){
            // Prints move suggestion from stockfish
            val bestMoveText by chessGameViewModel.bestMoveText.collectAsState()
            Text(
                text = bestMoveText
            )
        }
    }
}



