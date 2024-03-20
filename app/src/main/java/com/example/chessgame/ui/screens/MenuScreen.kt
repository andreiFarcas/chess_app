package com.example.chessgame.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chessgame.R

import com.example.chessgame.ui.theme.ChessGameTheme

@Composable
fun MenuScreen(
    onPlayButtonClicked: () -> Unit,
    onPuzzlesButtonClicked: () -> Unit,
    onAboutButtonClicked: () -> Unit,

    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.chess_app_icon),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.padding_large))
                .clip(RoundedCornerShape(18.dp)) // Apply rounded corners
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.SpaceBetween // Ensures space between the elements of the menu
        ){
            MainMenuButton(
                labelResource = "Play",
                onClick = onPlayButtonClicked
            )
            MainMenuButton(
                labelResource = "Puzzles",
                onClick = onPuzzlesButtonClicked
            )
            MainMenuButton(
                labelResource = "About",
                onClick = onAboutButtonClicked
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
    }
}

@Composable
fun MainMenuButton(
    labelResource: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()// Ensures each button has same width
            .padding(start = 36.dp, end = 36.dp)
    ) {
        Text(labelResource)
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    ChessGameTheme {
        MenuScreen(
            onPlayButtonClicked = {},
            onPuzzlesButtonClicked = {},
            onAboutButtonClicked = {}
        )
    }
}