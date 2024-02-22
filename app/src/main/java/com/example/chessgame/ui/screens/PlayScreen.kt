package com.example.chessgame.ui.screens


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel   // Import not suggested by the IDE !!
import com.example.chessgame.R
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.components.ChessBoardUi


@Composable
fun PlayScreen(
    chessGameViewModel: ChessGameViewModel = viewModel(),
){
    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()
    Column() {
        Text(
            text = "Here you will be able to play chess",
            modifier = Modifier.padding(end = dimensionResource(id = R.dimen.padding_small))
        )
        ChessBoardUi(
            chessGameViewModel = chessGameViewModel,
            piecesState = boardState.piecesState,
            possibleMoves = boardState.possibleMoves,
            clickedSquare = boardState.clickedSquare
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChessBoardPreview(){
    PlayScreen()
}


