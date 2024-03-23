package com.example.chessgame.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessgame.ui.components.ChessBoardUi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chessgame.ui.ChessGameViewModel

@Composable
fun PuzzlesScreen(
    chessGameViewModel: ChessGameViewModel = viewModel(),
){
    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()

    Column {
        Text(
            text = "Here you will be able to train your brain!"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PuzzlesScreenPreview(){
    PuzzlesScreen()
}