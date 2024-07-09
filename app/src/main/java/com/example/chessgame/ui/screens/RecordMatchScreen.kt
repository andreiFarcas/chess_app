package com.example.chessgame.ui.screens

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.components.ChessBoardUi
import com.example.chessgame.ui.components.TopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordMatchScreen(
    context: Context,
    chessGameViewModel: ChessGameViewModel = viewModel(),
    navController: NavController,
) {
    chessGameViewModel.setPlayVsStockfish(false) // Make sure Stockfish doesn't interfere with the game

    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var recordingName by remember { mutableStateOf("") }
    var showFiles by remember { mutableStateOf(false) }
    var selectedFile: File? by remember { mutableStateOf(null) }
    val isRecording = boardState.recordingGame
    val wasRecording by remember {
        mutableStateOf(isRecording)
    }

    LaunchedEffect(isRecording) {
        if(isRecording && !wasRecording){
            withContext(Dispatchers.IO) {
                Log.d("Recording", "moveFromChessboard called on isRecording changed value form recordingScreen")
                chessGameViewModel.moveFromChessboard()
            }
        }
    }

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
                text = "HINT: Place the pieces on initial position and press the start recording button!",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
            ChessBoardUi(
                chessGameViewModel = chessGameViewModel,
                piecesState = boardState.piecesState,
                possibleMoves = boardState.possibleMoves,
                clickedSquare = boardState.clickedSquare,
                bKingInCheck = boardState.bKingInCheck,
            )

            // Start and Stop Recording buttons
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { chessGameViewModel.startRecording() },
                    enabled = !isRecording,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6aa84f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        //.alpha(if (!isRecording) 1f else 0.9f)
                        .weight(0.485f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Recording"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Start Recording")
                }
                Spacer(modifier = Modifier.weight(0.03f))
                Button(
                    onClick = { showDialog = true },
                    enabled = isRecording,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFb01d1d),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        //.alpha(if (isRecording) 1f else 0.9f)
                        .weight(0.485f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Stop Recording"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Stop Recording")
                }
            }
            if (isRecording) {
                Text("Recorded Moves: ", fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    boardState.moves.forEachIndexed { index, move ->
                        val moveText = try {
                            chessGameViewModel.moveConversion(move, index + 1)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            "Error converting move"
                        }

                        // Special formatting for current move
                        val textColor =
                            if (index == boardState.currentMove - 1) Color.Red else Color.Black

                        Text(
                            text = moveText,
                            color = textColor
                        )

                        if (index < boardState.moves.size - 1) {
                            Text(text = "; ")
                        }
                    }
                }
                Text("Recorded Captures (move, piece): ", fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(boardState.captures.toString())
                }
            }

        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Stop Recording") },
                text = {
                    Column {
                        Text(text = "Enter name for the recording:")
                        TextField(
                            value = recordingName,
                            onValueChange = { recordingName = it }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        chessGameViewModel.saveRecording(context, recordingName)
                        showDialog = false
                    }) {
                        Text("Save Recording")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        chessGameViewModel.resetRecording()
                        showDialog = false
                    }) {
                        Text("Reset without saving")
                    }
                }
            )
        }

        if (showFiles) {
            val recordedFiles = chessGameViewModel.getAllRecordingFiles(context)

            Column {
                Text("Select a recorded game:")
                Spacer(modifier = Modifier.height(8.dp))

                if (recordedFiles.isEmpty()) {
                    Text("No recorded games found.")
                } else {
                    LazyColumn {
                        items(recordedFiles) { file ->
                            Button(
                                onClick = {
                                    selectedFile = file
                                    showFiles = false // Close the file selection view
                                    // Call function to load moves from selected file
                                    chessGameViewModel.loadMovesFromFile(file.toString())
                                },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(file.name)
                            }
                        }
                    }
                }
            }
        }
    }
}