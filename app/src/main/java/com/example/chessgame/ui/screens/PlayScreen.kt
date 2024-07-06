package com.example.chessgame.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chessgame.R
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.components.ChessBoardUi
import com.example.chessgame.ui.components.TopBar
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
    navController: NavController,
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

    Scaffold(
        topBar = {
            TopBar() {
                navController.popBackStack()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ){
            Text(
                text = "\nMove counter: ${boardState.moveCounter}",
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )
            Text(
                text = "Stockfish level: ${boardState.difficultyStockfish}",
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            )
            ChessBoardUi(
                chessGameViewModel = chessGameViewModel,
                piecesState = boardState.piecesState,
                possibleMoves = boardState.possibleMoves,
                clickedSquare = boardState.clickedSquare,
                bKingInCheck = boardState.bKingInCheck,
            )
            Button(
                onClick = {chessGameViewModel.resetBoard() },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(text = "Reset Game")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "\n FEN code for position:\n ${chessGameViewModel.testFenInterface()}\n",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.padding_small))
                    )

                    if (!chessGameViewModel.checkPlayVsStockfish()) {
                        // Prints move suggestion from stockfish (Used on practice screen)
                        val bestMoveText by chessGameViewModel.bestMoveText.collectAsState()
                        Text(
                            text = bestMoveText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.padding_small)),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

        }
    }
}



