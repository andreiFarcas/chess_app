package com.example.chessgame.ui.screens

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chessgame.R
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.components.ChessBoardUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WatchRecordingScreen(
    context: Context,
    chessGameViewModel: ChessGameViewModel = viewModel(),
){
    chessGameViewModel.setPlayVsStockfish(false) // Make sure Stockfish doesn't interfere with the game
    
    val boardState by chessGameViewModel.chessBoardUiState.collectAsState()
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            chessGameViewModel.moveFromChessboard()
        }
    }

    Column {
        SelectFileList(
            onFileSelected = { selectedFile = it },
            selectedFile = selectedFile,
            chessGameViewModel = chessGameViewModel,
            context = context,
            modifier = Modifier
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.padding_large),
                    end = dimensionResource(R.dimen.padding_large)
                )
        ) {
            Button(
                onClick = { chessGameViewModel.loadMovesFromFile(selectedFile.toString()) },
                modifier = Modifier
                    .padding(start = dimensionResource(id = R.dimen.padding_small))
                    .weight(0.75f)
                    .fillMaxWidth()
            ) {
                Text(text = "Load")
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .padding(end = dimensionResource(id = R.dimen.padding_small))
                    .weight(0.25f)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
        ChessBoardUi(
            chessGameViewModel = chessGameViewModel,
            piecesState = boardState.piecesState,
            possibleMoves = boardState.possibleMoves,
            clickedSquare = boardState.clickedSquare,
            bKingInCheck = boardState.bKingInCheck,
        )
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Button(onClick = { chessGameViewModel.moveBack() }) {
                Text(text = "<")
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Button(onClick = { chessGameViewModel.moveForward() }) {
                Text(text = ">")
            }
        }
        Box(modifier = Modifier.fillMaxWidth()){
            if (selectedFile == null || boardState.moves.isEmpty()) {
                Text(text = "Please load a file first!")
            } else {
                FlowRow(modifier = Modifier.fillMaxWidth()) {
                    boardState.moves.forEachIndexed { index, move ->
                        val moveText = try {
                            chessGameViewModel.moveConversion(move, index+1)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            "Error converting move"
                        }

                        // Special formatting for current move
                        val textColor = if (index == boardState.currentMove - 1) Color.Red else Color.Black

                        Text(
                            text = moveText,
                            color = textColor
                        )

                        if (index < boardState.moves.size - 1) {
                            Text(text = "; ")
                        }
                    }
                    Text("Captures (move, piece):")
                    Text(boardState.captures.toString())
                }
            }
        }

        if (showDialog) {
            DeleteDialog(
                showDialog = showDialog,
                onDismiss = { showDialog = false },
                onConfirm = {
                    val deleted = chessGameViewModel.deleteFile(selectedFile.toString())
                    if (deleted) {
                        Toast.makeText(context, "File deleted successfully", Toast.LENGTH_SHORT)
                            .show()
                        selectedFile = null
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun DeleteDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete the selected file?") },
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

// A button that will look like the other ones but when pressed will expand to offer more options
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectFileList(onFileSelected: (File) -> Unit, selectedFile: File?, chessGameViewModel: ChessGameViewModel, context: Context, modifier: Modifier) {
    val expanded = remember { mutableStateOf(false) } // Tracks if the dropdown is expanded

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.padding_large),
                end = dimensionResource(R.dimen.padding_large)
            )
    ) {
        Button(
            onClick = { expanded.value = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen.padding_small),
                    end = dimensionResource(id = R.dimen.padding_small)
                )
        ) {
            Text(
                text = selectedFile?.name ?: "Please select a file",
            )
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            val recordedFiles = chessGameViewModel.getAllRecordingFiles(context)
            if (recordedFiles.isEmpty()) {
                Text("No recorded games found.")
            } else {
                recordedFiles.forEach { file ->
                    DropdownMenuItem(
                        text = { Text(file.name) },
                        onClick = {
                            onFileSelected(file)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}