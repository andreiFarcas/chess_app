package com.example.chessgame.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chessgame.R
import com.example.chessgame.ui.ChessGameViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PuzzlesScreen(
    chessGameViewModel: ChessGameViewModel = viewModel(),
){
    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "COMING SOON...",
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight(48),
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small)),
            )
            Image(
                painter = painterResource(id = R.drawable.puzzles),
                contentDescription = "Moving Pawn drawing",
                modifier = Modifier
                    .size(150.dp) // Adjust the size as needed
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PuzzlesScreenPreview(){
    PuzzlesScreen()
}