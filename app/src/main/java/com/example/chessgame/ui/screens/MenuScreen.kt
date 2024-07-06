package com.example.chessgame.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chessgame.R
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.chessgame.interfaces.BluetoothManager

/*

    Function used to build all the elements from the Main Screen of the app

 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuScreen(
    navController: NavController,
    //onPlayButtonClicked: (String) -> Unit,  // replaced by direct navcontroler.navigate call
    onPuzzlesButtonClicked: () -> Unit,
    onRecordButtonClicked: () -> Unit,
    onWatchButtonClicked: () -> Unit,
    onPracticeButtonClicked: () -> Unit,
    onAboutButtonClicked: () -> Unit,
    bluetoothManager: BluetoothManager,
    modifier: Modifier = Modifier
){
    // Tracks changes in connection with the board
    val isBoardConnected = bluetoothManager.connectionStatus.collectAsState(initial = false).value

    // Remembers if Are you sure? dialog is shown or not
    var showDisconnectionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        BoardConnectionRow(
            isBoardConnected = isBoardConnected,
            onConnectClicked = { bluetoothManager.searchForDevices() },
            onDisconnectClicked = { showDisconnectionDialog = true },
            showDisconnectionDialog = showDisconnectionDialog,
            setShowDisconnectionDialog = { showDisconnectionDialog = it },
            bluetoothManager = bluetoothManager
        )

        ChessLogo(modifier)

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_large)))

        MenuOptions(
            navController = navController,
            onPracticeButtonClicked = onPracticeButtonClicked,
            onRecordButtonClicked = onRecordButtonClicked,
            onWatchButtonClicked = onWatchButtonClicked,
            onAboutButtonClicked = onAboutButtonClicked,
            onPuzzlesButtonClicked = onPuzzlesButtonClicked,
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
    }

}


@Composable
fun MenuOptions(
    navController: NavController,
    onPracticeButtonClicked: () -> Unit,
    onRecordButtonClicked: () -> Unit,
    onWatchButtonClicked: () -> Unit,
    onAboutButtonClicked: () -> Unit,
    onPuzzlesButtonClicked: () -> Unit,
    modifier: Modifier
){
    Column(
        modifier = Modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween // Ensures space between the elements of the menu
    ){
        MainMenuDropDown(
            labelResource = "Play",
            onClick = { difficulty ->
                navController.navigate(ChessGameScreen.Play.name + "/$difficulty")
            }
        )
        MainMenuButton(
            labelResource = "Record Game",
            onClick = onRecordButtonClicked
        )
        MainMenuButton(
            labelResource = "Watch Record",
            onClick = onWatchButtonClicked
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
        Spacer(modifier = Modifier.height(46.dp))
        Image(
            painter = painterResource(id = R.drawable.utcn_logo),
            contentDescription = "UTCN logo",
            modifier = Modifier
                .size(90.dp) // Adjust the size as needed
                .align(Alignment.CenterHorizontally) // Center the image horizontally
                .padding(bottom = dimensionResource(id = R.dimen.padding_small)) // Add padding if necessary
        )
    }
}

@Composable
fun ChessLogo(modifier: Modifier){
    Box(
        modifier = modifier
            .height(275.dp)
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_large))
    ){
        Image(
            painter = painterResource(id = R.drawable.chess_app_icon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(18.dp)) // Apply rounded corners
        )
    }
}

// Row with: Button to connect/disconnect and status of connected/disconnected
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BoardConnectionRow(
    isBoardConnected: Boolean,
    onConnectClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    showDisconnectionDialog: Boolean,
    setShowDisconnectionDialog: (Boolean) -> Unit,
    bluetoothManager: BluetoothManager
) {
    Row(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
    ) {
        if (!isBoardConnected) {
            Button(
                onClick = onConnectClicked,
            ) {
                Text(text = "Connect Chessboard")
            }

            Spacer(Modifier.weight(1f)) // This pushes the next text item to the end

            Text(
                text = "Not connected",
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        } else {
            Button(
                onClick = { setShowDisconnectionDialog(true) },
            ) {
                Text(text = "Disconnect Chessboard")
            }

            // Place the disconnection dialog call here if needed or handle it outside

            Spacer(Modifier.weight(1f))

            Text(
                text = "Connected",
                color = Color.Green,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }

    // Composable that shows an "Are you sure?" dialog
    if (showDisconnectionDialog) {
        DisconnectDialog(
            showDialog = showDisconnectionDialog,
            onDismiss = { setShowDisconnectionDialog(false) },
            onConfirm = {
                bluetoothManager.disconnectDevice()
                setShowDisconnectionDialog(false)
            }
        )
    }
}

// Dialog called when pressed disconnect button
@Composable
fun DisconnectDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Disconnect Chessboard") },
            text = { Text("Are you sure you want to disconnect the chessboard?") },
            confirmButton = {
                Button(
                    onClick = onConfirm
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("No")
                }
            }
        )
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
) {
    val expandedButton = remember { mutableStateOf(false) }
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
    ) {
        Button(
            onClick = { expandedButton.value = !expandedButton.value },
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.padding_large),
                    end = dimensionResource(R.dimen.padding_large)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = labelResource, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Icon(
                    imageVector = if (expandedButton.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expandedButton.value) "Open" else "Close menu"
                )
            }
        }

        // Annimated play button that is expandable based on expandedButton
        AnimatedVisibility(
            visible = expandedButton.value,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkOut(shrinkTowards = Alignment.TopStart) + fadeOut()
        ) {
            Column {
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
                ) {
                    Text("Start")
                }
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
        FilledTonalButton(
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
        FilledTonalButton(
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
            DropdownMenuItem(
                text = { Text("Professional") },
                onClick = {
                    selectedDifficulty.value = "Professional"
                    expanded.value = false
                }
            )
        }
    }
}