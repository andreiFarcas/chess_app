package com.example.chessgame.ui

import androidx.lifecycle.ViewModel
import com.example.chessgame.data.ChessBoardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ChessGameViewModel holds information regarding the pieces on the chessboard

class ChessGameViewModel : ViewModel() {
    private val _chessBoardUiState = MutableStateFlow(ChessBoardState())  // A matrix of
    val chessBoardUiState: StateFlow<ChessBoardState> = _chessBoardUiState.asStateFlow()

    private fun resetBoard(){
        _chessBoardUiState.update { ChessBoardState() }   // TO DO
    }

    fun movePiece(fromRow: Int, fromColumn: Int, toRow: Int, toColumn: Int) {
        val currentBoardState = _chessBoardUiState.value
        val pieceToMove = currentBoardState.piecesState[fromRow][fromColumn] // String code of piece to move

        // Create a new List<List<>> with 8 elements but move the piece
        val newPiecesState = mutableListOf<List<String>>()

        for (i in currentBoardState.piecesState.indices) {
            val newRow = mutableListOf<String>()
            for (j in currentBoardState.piecesState[i].indices) {
                if (i == toRow && j == toColumn) {
                    // We are at the destination position -> we add the piece
                    newRow.add(pieceToMove)
                } else if (i == fromRow && j == fromColumn) {
                    // We are at the initial position -> we remove the piece
                    newRow.add("")
                } else {
                    // We don't need to modify anything -> just copy
                    newRow.add(currentBoardState.piecesState[i][j])
                }
            }
            newPiecesState.add(newRow) // Add the row to the newPiecesState
        }

        val newBoardState = ChessBoardState(piecesState = newPiecesState)

        // Update the ChessBoardState with the new board state
        _chessBoardUiState.value = newBoardState

    }
    init{
        resetBoard()
    }
}