package com.example.chessgame.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chessgame.R
import com.example.chessgame.data.DataSource
import com.example.chessgame.ui.ChessGameViewModel


// Composable that draws a chessboard, receives an 8x8 list of pieces codes
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChessBoardUi(
    chessGameViewModel: ChessGameViewModel,
    piecesState: List<List<String>>,
    clickedSquare: Pair<Int, Int>,
    possibleMoves: List<Pair<Int, Int>>,
    bKingInCheck: Boolean,
){
    Box(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)  // Box of square shape representing the chessboard size
        .padding(dimensionResource(id = R.dimen.padding_small))
    ){
        // The board has a column layout (one column of 8 rows)
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ){
            for(i in 0..7){
                // We have 8 rows
                Row(
                    modifier = Modifier.weight(1f) // All rows will take the same size in the Column
                ) {
                    for(j in 0..7){
                        val isGreenSquare = (i + j) % 2 == 0 // Based on parity of indexes we color dark or light

                        // Add chessboard notations:
                        var letter = ' '
                        var number = ' '
                        if(i == 7)
                            letter = 'a' + j
                        if(j == 7)
                            number = '0' + (8 - i)

                        val squareColor = if(isGreenSquare) Color.Green else Color.Gray

                        // We check what piece we have on that square
                        val piece: String = piecesState[i][j]

                        // Get the right image for the piece, or 0 if there is no piece
                        val imageResource = DataSource.piecesImages[piece] ?: 0

                        // Boolean that says if current square is clicked or not
                        val isSquareClicked = (clickedSquare == Pair(i, j))

                        // Boolean that says if current square is possible move or not
                        val isPossibleMove = possibleMoves.contains(Pair(i, j))

                        // Booleans that says if current square is a king in check or not
                        val isKingInCheck = (piece.contains("bK") && bKingInCheck)

                        // We have 8 squares / row
                        ChessSquareUi(
                            notationLetter = letter,
                            notationNumber = number,
                            imageResource = imageResource,
                            onClick = {
                                chessGameViewModel.onClickSquare(Pair(i, j))
                            },
                            isPossibleMove = isPossibleMove,
                            isClicked = isSquareClicked,
                            isKingInCheck = isKingInCheck,
                            modifier = Modifier
                                .weight(1f) // All will have equal sizes in the Row
                                .fillMaxSize()
                                .background(squareColor)
                        )
                    }
                }
            }
        }
    }
}

// Function that draws one chess square
@Composable
fun ChessSquareUi(
    onClick: () -> Unit,
    isClicked: Boolean,
    isPossibleMove: Boolean,
    imageResource: Int,
    isKingInCheck: Boolean,
    notationLetter: Char,
    notationNumber: Char,
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if(isKingInCheck) Color.Red
                else if (isClicked) Color.Yellow
                else if(isPossibleMove) Color.Blue
                else Color.Transparent)
            .clickable {onClick()}
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
            //.shadow(1.dp)
        ){
            if(imageResource != 0){
                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = null
                )
            }

            // Display the letter notation on the bottom left
            if (notationLetter != ' ') {
                Text(
                    text = notationLetter.toString(),
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.BottomStart).padding(1.dp)
                )
            }

            // Display the number notation on the top right
            if (notationNumber != ' ') {
                Text(
                    text = notationNumber.toString(),
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

