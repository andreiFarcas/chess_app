package com.example.chessgame.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessgame.R


@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Text(
            text = "This is my thesis project and it is still work in progress, it will only get better :)",
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "Chess pieces from:\nhttps://greenchess.net/info.php?item=downloads",
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "CoreXY system:\nhttps://corexy.com/theory.html",
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "Steppers: 28byj-48; Drivers: ulm2005",
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )
        Text(
            text = "Chess engine used: Stockfish",
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
