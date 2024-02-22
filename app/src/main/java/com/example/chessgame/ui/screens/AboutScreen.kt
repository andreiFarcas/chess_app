package com.example.chessgame.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun AboutScreen() {
    Column {
        Text(
            text = "This is my thesis project and it is still work in progress, it will only get better :)"
        )
        Text(
            text = "Chess pieces from: https://greenchess.net/info.php?item=downloads"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}
