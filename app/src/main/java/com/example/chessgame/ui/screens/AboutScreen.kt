package com.example.chessgame.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.chessgame.R
import com.example.chessgame.ui.components.TopBar


@Composable
fun AboutScreen(
    navController: NavController,
) {
    Scaffold(
        topBar = {
            TopBar() {
                navController.popBackStack()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
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
                text = "Steppers: 28byj-48; Drivers: ulm2005; Bluetooth: HC-05",
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
            )
            Text(
                text = "Chess engine used: Stockfish",
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}





