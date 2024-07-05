package com.example.chessgame.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chessgame.ui.ChessGameViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PuzzlesScreen(
    chessGameViewModel: ChessGameViewModel = viewModel(),
){
    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()

    Column {
        Text(
            text = "Place the pieces on initial position and press the start recording button"
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PuzzlesScreenPreview(){
    PuzzlesScreen()
}