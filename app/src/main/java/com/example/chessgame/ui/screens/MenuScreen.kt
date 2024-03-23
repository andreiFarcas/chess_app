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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chessgame.R
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.chessgame.ui.theme.ChessGameTheme

/*

    Function used to build all the elements from the Main Screen of the app

 */

@Composable
fun MenuScreen(
    navController: NavController,
    //onPlayButtonClicked: (String) -> Unit,  // replaced by direct navcontroler.navigate call
    onPuzzlesButtonClicked: () -> Unit,
    onPracticeButtonClicked: () -> Unit,
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
            MainMenuDropDown(
                labelResource = "Play",
                onClick = { difficulty ->
                    navController.navigate(ChessGameScreen.Play.name + "/$difficulty")
                }
            )
            MainMenuButton(
                labelResource = "Puzzles",
                onClick = onPuzzlesButtonClicked
            )
            MainMenuButton(
                labelResource = "Practice",
                onClick = onPracticeButtonClicked
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
            .padding(
                start = dimensionResource(R.dimen.padding_large),
                end = dimensionResource(R.dimen.padding_large)
            )
    ) {
        Text(labelResource)
    }
}

// A button that will look like the other ones but when pressed will expand to offer more options
@Composable
fun MainMenuDropDown(
    labelResource: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
){
    val expandedButton = remember{mutableStateOf(false)}
    val selectedDifficulty = remember { mutableStateOf("Select Difficulty") } // Tracks the selected difficulty
    val selectedColor = remember { mutableStateOf("Select Color") }

    // Prepare the card properties based on the expansion state
    val cardColors = if (expandedButton.value) {
        // When expanded make background gray
        CardDefaults.cardColors(
            containerColor = Color.LightGray,
            contentColor = contentColorFor(backgroundColor = Color.LightGray)
        )
    } else {
        // When not expanded keep it transparent
        CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = contentColorFor(backgroundColor = Color.Transparent)
        )
    }

    Card(
        modifier = modifier,
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    )
    {
        Button(
            onClick = { expandedButton.value = !expandedButton.value },
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.padding_large),
                    end = dimensionResource(R.dimen.padding_large)
                )
        ) {
            Text(text = labelResource)
        }
        if(expandedButton.value){
            SelectDifficulty(selectedDifficulty, modifier)
            SelectColor(selectedColor, modifier)

            Button(
                onClick = { onClick(selectedDifficulty.value) },
                modifier = modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        start = dimensionResource(R.dimen.padding_large),
                        end = dimensionResource(R.dimen.padding_large)
                    ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            ){
                Text("Start")
            }
        }
    }
}

@Composable
fun SelectColor(selectedColor: MutableState<String>, modifier: Modifier){
    val expanded = remember { mutableStateOf(false) } // Tracks if the dropdown is expanded

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(
            start = dimensionResource(R.dimen.padding_large),
            end = dimensionResource(R.dimen.padding_large)
        )) {
        Text(
            text = "Please select color: ",
            modifier = modifier.padding(
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_small)
            )
        )
        Button(
            onClick = { expanded.value = true },
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
        ){
            Text(text = selectedColor.value)
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = modifier.fillMaxWidth()
        ) {
            DropdownMenuItem(
                text = { Text("White") },
                onClick = {
                    selectedColor.value = "White"
                    expanded.value = false
                }
            )
            DropdownMenuItem(
                text = { Text("Black") },
                onClick = {
                    selectedColor.value = "Black"
                    expanded.value = false
                }
            )
            DropdownMenuItem(
                text = { Text("Random") },
                onClick = {
                    selectedColor.value = "Random"
                    expanded.value = false
                }
            )
        }
    }
}

@Composable
fun SelectDifficulty(selectedDifficulty: MutableState<String>, modifier: Modifier){
    val expanded = remember { mutableStateOf(false) } // Tracks if the dropdown is expanded

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(
            start = dimensionResource(R.dimen.padding_large),
            end = dimensionResource(R.dimen.padding_large)
        )) {
        Text(
            text = "Please select difficulty: ",
            modifier = modifier.padding(
                start = dimensionResource(id = R.dimen.padding_small),
                end = dimensionResource(id = R.dimen.padding_small)
            )
        )
        Button(
            onClick = { expanded.value = true },
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
        ){
            Text(text = selectedDifficulty.value)
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            DropdownMenuItem(
                text = { Text("Easy") },
                onClick = {
                    selectedDifficulty.value = "Easy"
                    expanded.value = false
                }
            )
            DropdownMenuItem(
                text = { Text("Medium") },
                onClick = {
                    selectedDifficulty.value = "Medium"
                    expanded.value = false
                }
            )
            DropdownMenuItem(
                text = { Text("Hard") },
                onClick = {
                    selectedDifficulty.value = "Hard"
                    expanded.value = false
                }
            )
        }
    }
}