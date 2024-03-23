package com.example.chessgame.ui

import kotlinx.coroutines.*
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessgame.data.ChessBoardState
import com.example.chessgame.engine.ChessEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/*

 ChessGameViewModel is responsible to react and provide information for and after all UI interactions

 */

class ChessGameViewModel(private val chessEngine: ChessEngine) : ViewModel() {

    private val _chessBoardUiState = MutableStateFlow(ChessBoardState())
    val chessBoardUiState: StateFlow<ChessBoardState> = _chessBoardUiState.asStateFlow()

    fun checkPlayVsStockfish(): Boolean{
        return _chessBoardUiState.value.playVsStockfish
    }
    fun setPlayVsStockfish(boolValue: Boolean) {
        val currentState = _chessBoardUiState.value
        if(currentState.playVsStockfish != boolValue){
            _chessBoardUiState.value = currentState.copy(playVsStockfish = boolValue)
        }
    }

    fun testFenInterface(): String {
        val currentBoardState = _chessBoardUiState.value

        return chessEngine.getFenFromChessBoardState(currentBoardState)
    }

    private val _bestMoveText = MutableStateFlow("Waiting for Stockfish...")
    val bestMoveText: StateFlow<String> = _bestMoveText.asStateFlow() // Will be read in practice mode

    fun findBestMoveByStockfish() {
        viewModelScope.launch {
            // Perform the move calculation on a background thread
            withContext(Dispatchers.IO) {
                val currentBoardState = _chessBoardUiState.value
                val fen = chessEngine.getFenFromChessBoardState(currentBoardState)

                val bestMove = chessEngine.getBestMove(fen, 15)

                _bestMoveText.value = "Stockfish suggested move:\n$bestMove"
            }
        }
    }

    fun setDifficultyLevelStockfish(difficulty: String) {
        Log.d("Level", "Level set to: $difficulty")
        _chessBoardUiState.update { currentState ->
            when(difficulty) {
                "Easy" -> currentState.copy(difficultyStockfish = 1)
                "Medium" -> currentState.copy(difficultyStockfish = 3)
                "Hard" -> currentState.copy(difficultyStockfish = 6)
                else -> currentState // No change if the difficulty string doesn't match
            }
        }
    }

    fun moveByStockfish() {
        // Happens in a coroutine because don't want the execution of the rest of the app to be
        // affected by Stockfish computation time
        viewModelScope.launch {
            val currentBoardState = _chessBoardUiState.value
            val fen = chessEngine.getFenFromChessBoardState(currentBoardState)

            // Perform the move calculation on a background thread
            withContext(Dispatchers.IO) {
                val bestMove: String? = chessEngine.getBestMove(fen, currentBoardState.difficultyStockfish)

                bestMove?.let {
                    // Convert from letter representation to normal square indexes
                    val initialSquare = Pair(8 - it[1].digitToInt(), letterToNumber(it[0]))
                    val targetSquare = Pair(8 - it[3].digitToInt(), letterToNumber(it[2]))

                    // Update the UI on the main thread
                    withContext(Dispatchers.Main) {
                        movePiece(initialSquare.first, initialSquare.second, targetSquare.first, targetSquare.second)
                    }
                }
            }
        }
    }

    // Used to covert a move like e2e4 to regular indexes
    private fun letterToNumber(letter: Char): Int {
        return letter.code - 'a'.code  // basically maps a->7, b->6, c->5 etc...
    }

    // Function called whenever user interacts with the board
    fun onClickSquare(square: Pair<Int, Int>){
        val currentBoardState = _chessBoardUiState.value
        val isSquareClicked = (currentBoardState.clickedSquare == square)

        // If the clicked square is a possible move, move the selected piece there
        if(currentBoardState.possibleMoves.contains(square)){
            movePiece(currentBoardState.clickedSquare.first, currentBoardState.clickedSquare.second, square.first, square.second)
            resetClickedSquare()
            resetPossibleMoves()
        } else{
            // If square is not the clicked one then we mark it as clicked
            if(!isSquareClicked) {
                markAsClicked(square)
                resetPossibleMoves() // Remove existing possible moves markings
            }

            // We search for possible moves if we selected a piece
            if(currentBoardState.piecesState[square.first][square.second] != ""){
                markPossibleMoves(square, _chessBoardUiState.value, false)
            }
        }
    }

    // Function that resets board to initial state
    fun resetBoard(){
        val currentBoardState = _chessBoardUiState.value
        _chessBoardUiState.update { ChessBoardState(playVsStockfish = currentBoardState.playVsStockfish) }   // Rebuilds the board state
    }

    // Function that moves a piece from starting position to target position
    private fun movePiece(fromRow: Int, fromColumn: Int, toRow: Int, toColumn: Int) {
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

        val afterMoveBoardState = currentBoardState.copy(
            playVsStockfish = currentBoardState.playVsStockfish,
            piecesState = newPiecesState,
            whiteTurn = currentBoardState.whiteTurn,
            bKingInCheck = currentBoardState.bKingInCheck
        )

        var bKingInCheck = false
        var bKCoordinates = Pair(0, 0)
        if(afterMoveBoardState.whiteTurn){
            // Find the black king:
            for(i in 0..7){
                for(j in 0..7){
                    if(afterMoveBoardState.piecesState[i][j].contains("bK")){ // we have found our black king
                        bKCoordinates = Pair(i, j)
                    }
                }
            }

            // Check if we are checking opposing king after our move
            for(i in 0..7){
                for(j in 0..7){
                    if(afterMoveBoardState.piecesState[i][j].contains("w")) // we have a white piece
                    {
                        markPossibleMoves(Pair(i, j), afterMoveBoardState, true) // we mark all possible moves for white pieces
                        Log.d("CheckCheck", "${afterMoveBoardState.possibleMoves}")
                        if(afterMoveBoardState.possibleMoves.contains(bKCoordinates)){
                            // our black king is threatened
                            bKingInCheck = true
                            Log.d("CheckCheck", "Black King is checked")
                        }
                    }
                }
            }

            // Reset possible moves
            resetPossibleMoves()
        }

        // Increment the move counter only after black moves (for FEN)
        var moveNumber:Int = currentBoardState.moveCounter
        if(!currentBoardState.whiteTurn)
            moveNumber += 1

        val newBoardState = currentBoardState.copy(
            playVsStockfish = currentBoardState.playVsStockfish,
            piecesState = newPiecesState,
            whiteTurn = !currentBoardState.whiteTurn,
            bKingInCheck = bKingInCheck,
            moveCounter = moveNumber
        )

        // Update the ChessBoardState with the new board state
        _chessBoardUiState.value = newBoardState

        // If we play vs CPU and is not our turn let CPU move
        if(!newBoardState.whiteTurn && newBoardState.playVsStockfish)
            moveByStockfish()

        // If we are in practice mode, recompute the suggested move every time
        if(!newBoardState.playVsStockfish)
            findBestMoveByStockfish()
    }
    // Function that resets clicked square markings
    private fun resetClickedSquare(){
        _chessBoardUiState.update { currentBoardState ->
            currentBoardState.copy(clickedSquare = Pair(-1, -1))
        }
    }
    // Function that resets possible moves markings
    private fun resetPossibleMoves(){
        _chessBoardUiState.update { currentBoardState ->
            currentBoardState.copy(possibleMoves = listOf())
        }
    }
    // Function that marks a square as clicked
    private fun markAsClicked(square: Pair<Int, Int>) {
        _chessBoardUiState.update { currentBoardState ->
            currentBoardState.copy(clickedSquare = square)
        }
    }
    init{
        resetBoard()
    }

    // Function that checks if opposing king is in check, right after a move
    private fun checkForCheck(currentBoardState: ChessBoardState): Boolean{
        var bKingInCheck = false
        var bKCoordinates = Pair(0, 0)
        if(currentBoardState.whiteTurn){
            // Find the black king:
            for(i in 0..7){
                for(j in 0..7){
                    if(currentBoardState.piecesState[i][j].contains("bK")){ // we have found our black king
                        bKCoordinates = Pair(i, j)
                    }
                }
            }

            // Check if we are checking opposing king after our move
            for(i in 0..7){
                for(j in 0..7){
                    if(currentBoardState.piecesState[i][j].contains("w")) // we have a white piece
                    {
                        markPossibleMoves(Pair(i, j), currentBoardState, true) // we mark all possible moves for white pieces
                        Log.d("CheckCheck", "${currentBoardState.possibleMoves}")
                        if(currentBoardState.possibleMoves.contains(bKCoordinates)){
                            // our black king is threatened
                            bKingInCheck = true
                            Log.d("CheckCheck", "Black King is checked")
                        }
                    }
                }
            }

            // Reset possible moves
            resetPossibleMoves()
        }
        return bKingInCheck
    }
    // Function that marks all possible moves for a piece
    private fun markPossibleMoves(selectedSquare: Pair<Int, Int>, currentBoardState: ChessBoardState, addOnOldPossibleMoves: Boolean) {

        val i = selectedSquare.first
        val j = selectedSquare.second

        // List of possible moves which will be added to the board state
        val possibleMoves: MutableList<Pair<Int, Int>> = if (addOnOldPossibleMoves) {
            currentBoardState.possibleMoves.toMutableList()
        } else {
            mutableListOf()
        }

        when (currentBoardState.piecesState[i][j]) {
            "wP" -> {
                if(currentBoardState.whiteTurn){
                    if (i - 1 >= 0 && currentBoardState.piecesState[i - 1][j] == "")
                        possibleMoves.add(Pair(i - 1, j))
                    if (i - 1 >= 0 && j - 1 >= 0 && currentBoardState.piecesState[i - 1][j - 1].contains("b"))
                        possibleMoves.add(Pair(i - 1, j - 1))
                    if (i - 1 >= 0 && j + 1 < 8 && currentBoardState.piecesState[i - 1][j + 1].contains("b"))
                        possibleMoves.add(Pair(i - 1, j + 1))
                    if (i == 6 && currentBoardState.piecesState[i - 2][j] == "")
                        possibleMoves.add(Pair(i - 2, j))
                }
            }

            "bP" -> {
                if(!currentBoardState.whiteTurn){
                    if (i + 1 < 8 && currentBoardState.piecesState[i + 1][j] == "")
                        possibleMoves.add(Pair(i + 1, j))
                    if (i + 1 < 8 && j - 1 >= 0 && currentBoardState.piecesState[i + 1][j - 1].contains("w"))
                        possibleMoves.add(Pair(i + 1, j - 1))
                    if (i + 1 < 8 && j + 1 < 8 && currentBoardState.piecesState[i + 1][j + 1].contains("w"))
                        possibleMoves.add(Pair(i + 1, j + 1))
                    if (i == 1 && currentBoardState.piecesState[i + 2][j] == "")
                        possibleMoves.add(Pair(i + 2, j))
                }
            }

            "wN" -> {
                if(currentBoardState.whiteTurn){
                    if (i + 1 < 8 && j + 2 < 8 && (currentBoardState.piecesState[i + 1][j + 2] == "" || currentBoardState.piecesState[i + 1][j + 2].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i + 1, j + 2))
                    if (i + 1 < 8 && j - 2 >= 0 && (currentBoardState.piecesState[i + 1][j - 2] == "" || currentBoardState.piecesState[i + 1][j - 2].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i + 1, j - 2))
                    if (i - 1 >= 0 && j + 2 < 8 && (currentBoardState.piecesState[i - 1][j + 2] == "" || currentBoardState.piecesState[i - 1][j + 2].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i - 1, j + 2))
                    if (i - 1 >= 0 && j - 2 >= 0 && (currentBoardState.piecesState[i - 1][j - 2] == "" || currentBoardState.piecesState[i - 1][j - 2].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i - 1, j - 2))
                    if (i + 2 < 8 && j + 1 < 8 && (currentBoardState.piecesState[i + 2][j + 1] == "" || currentBoardState.piecesState[i + 2][j + 1].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i + 2, j + 1))
                    if (i + 2 < 8 && j - 1 >= 0 && (currentBoardState.piecesState[i + 2][j - 1] == "" || currentBoardState.piecesState[i + 2][j - 1].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i + 2, j - 1))
                    if (i - 2 >= 0 && j + 1 < 8 && (currentBoardState.piecesState[i - 2][j + 1] == "" || currentBoardState.piecesState[i - 2][j + 1].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i - 2, j + 1))
                    if (i - 2 >= 0 && j - 1 >= 0 && (currentBoardState.piecesState[i - 2][j - 1] == "" || currentBoardState.piecesState[i - 2][j - 1].contains(
                            "b"
                        ))
                    )
                        possibleMoves.add(Pair(i - 2, j - 1))
                }
            }

            "bN" -> {
                if(!currentBoardState.whiteTurn){
                    if (i + 1 < 8 && j + 2 < 8 && (currentBoardState.piecesState[i + 1][j + 2] == "" || currentBoardState.piecesState[i + 1][j + 2].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i + 1, j + 2))
                    if (i + 1 < 8 && j - 2 >= 0 && (currentBoardState.piecesState[i + 1][j - 2] == "" || currentBoardState.piecesState[i + 1][j - 2].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i + 1, j - 2))
                    if (i - 1 >= 0 && j + 2 < 8 && (currentBoardState.piecesState[i - 1][j + 2] == "" || currentBoardState.piecesState[i - 1][j + 2].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i - 1, j + 2))
                    if (i - 1 >= 0 && j - 2 >= 0 && (currentBoardState.piecesState[i - 1][j - 2] == "" || currentBoardState.piecesState[i - 1][j - 2].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i - 1, j - 2))
                    if (i + 2 < 8 && j + 1 < 8 && (currentBoardState.piecesState[i + 2][j + 1] == "" || currentBoardState.piecesState[i + 2][j + 1].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i + 2, j + 1))
                    if (i + 2 < 8 && j - 1 >= 0 && (currentBoardState.piecesState[i + 2][j - 1] == "" || currentBoardState.piecesState[i + 2][j - 1].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i + 2, j - 1))
                    if (i - 2 >= 0 && j + 1 < 8 && (currentBoardState.piecesState[i - 2][j + 1] == "" || currentBoardState.piecesState[i - 2][j + 1].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i - 2, j + 1))
                    if (i - 2 >= 0 && j - 1 >= 0 && (currentBoardState.piecesState[i - 2][j - 1] == "" || currentBoardState.piecesState[i - 2][j - 1].contains(
                            "w"
                        ))
                    )
                        possibleMoves.add(Pair(i - 2, j - 1))
                }
            }

            "wB" -> {
                if(currentBoardState.whiteTurn){
                    // Diagonal movements (top-left to bottom-right)
                    var row = i - 1
                    var col = j - 1
                    while (row >= 0 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row--
                        col--
                    }
                    // Diagonal movements (top-right to bottom-left)
                    row = i - 1
                    col = j + 1
                    while (row >= 0 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row--
                        col++
                    }
                    // Diagonal movements (bottom-left to top-right)
                    row = i + 1
                    col = j - 1
                    while (row < 8 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row++
                        col--
                    }
                    // Diagonal movements (bottom-right to top-left)
                    row = i + 1
                    col = j + 1
                    while (row < 8 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row++
                        col++
                    }
                }
            }

            "bB" -> {
                if(!currentBoardState.whiteTurn){
                    // Diagonal movements (top-left to bottom-right)
                    var row = i - 1
                    var col = j - 1
                    while (row >= 0 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row--
                        col--
                    }

                    // Diagonal movements (top-right to bottom-left)
                    row = i - 1
                    col = j + 1
                    while (row >= 0 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row--
                        col++
                    }

                    // Diagonal movements (bottom-left to top-right)
                    row = i + 1
                    col = j - 1
                    while (row < 8 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row++
                        col--
                    }

                    // Diagonal movements (bottom-right to top-left)
                    row = i + 1
                    col = j + 1
                    while (row < 8 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row++
                        col++
                    }
                }
            }

            "wQ" -> {
                if(currentBoardState.whiteTurn){
                    // Horizontal movements (left)
                    for (col in j - 1 downTo 0) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("b")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }
                    // Horizontal movements (right)
                    for (col in j + 1 until 8) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("b")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }
                    // Vertical movements (up)
                    for (row in i - 1 downTo 0) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("b")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (down)
                    for (row in i + 1 until 8) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("b")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }

                    // Diagonal movements (top-left to bottom-right)
                    var row = i - 1
                    var col = j - 1
                    while (row >= 0 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row--
                        col--
                    }

                    // Diagonal movements (top-right to bottom-left)
                    row = i - 1
                    col = j + 1
                    while (row >= 0 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row--
                        col++
                    }

                    // Diagonal movements (bottom-left to top-right)
                    row = i + 1
                    col = j - 1
                    while (row < 8 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row++
                        col--
                    }

                    // Diagonal movements (bottom-right to top-left)
                    row = i + 1
                    col = j + 1
                    while (row < 8 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "b"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("b")) break
                        row++
                        col++
                    }
                }
            }

            "bQ" -> {
                if(!currentBoardState.whiteTurn) {
                    // Horizontal movements (left)
                    for (col in j - 1 downTo 0) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("w")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }

                    // Horizontal movements (right)
                    for (col in j + 1 until 8) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("w")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (up)
                    for (row in i - 1 downTo 0) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("w")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (down)
                    for (row in i + 1 until 8) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("w")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }

                    // Diagonal movements (top-left to bottom-right)
                    var row = i - 1
                    var col = j - 1
                    while (row >= 0 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row--
                        col--
                    }

                    // Diagonal movements (top-right to bottom-left)
                    row = i - 1
                    col = j + 1
                    while (row >= 0 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row--
                        col++
                    }

                    // Diagonal movements (bottom-left to top-right)
                    row = i + 1
                    col = j - 1
                    while (row < 8 && col >= 0 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row++
                        col--
                    }

                    // Diagonal movements (bottom-right to top-left)
                    row = i + 1
                    col = j + 1
                    while (row < 8 && col < 8 && (currentBoardState.piecesState[row][col] == "" || currentBoardState.piecesState[row][col].contains(
                            "w"
                        ))
                    ) {
                        possibleMoves.add(Pair(row, col))
                        if (currentBoardState.piecesState[row][col].contains("w")) break
                        row++
                        col++
                    }
                }
            }

            "bR" -> {
                if(!currentBoardState.whiteTurn){
                    // Horizontal movements (left)
                    for (col in j - 1 downTo 0) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("w")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }

                    // Horizontal movements (right)
                    for (col in j + 1 until 8) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("w")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (up)
                    for (row in i - 1 downTo 0) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("w")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (down)
                    for (row in i + 1 until 8) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("w")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }
                }
            }

            "wR" -> {
                if(currentBoardState.whiteTurn){
                    // Horizontal movements (left)
                    for (col in j - 1 downTo 0) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("b")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }

                    // Horizontal movements (right)
                    for (col in j + 1 until 8) {
                        if (currentBoardState.piecesState[i][col] == "") {
                            possibleMoves.add(Pair(i, col))
                        } else if (currentBoardState.piecesState[i][col].contains("b")) {
                            possibleMoves.add(Pair(i, col))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (up)
                    for (row in i - 1 downTo 0) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("b")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }

                    // Vertical movements (down)
                    for (row in i + 1 until 8) {
                        if (currentBoardState.piecesState[row][j] == "") {
                            possibleMoves.add(Pair(row, j))
                        } else if (currentBoardState.piecesState[row][j].contains("b")) {
                            possibleMoves.add(Pair(row, j))
                            break
                        } else {
                            break
                        }
                    }
                }
            }

            "wK" -> {
                if(currentBoardState.whiteTurn){
                    // Check all surrounding squares
                    for (rowOffset in -1..1) {
                        for (colOffset in -1..1) {
                            val newRow = i + rowOffset
                            val newCol = j + colOffset

                            // Check if the new position is within bounds and not occupied by a white piece
                            if (newRow in 0 until 8 && newCol in 0 until 8 && !currentBoardState.piecesState[newRow][newCol].contains("w")) {
                                possibleMoves.add(Pair(newRow, newCol))
                            }
                        }
                    }
                }
            }

            "bK" -> {
                if(!currentBoardState.whiteTurn){
                    // Check all surrounding squares
                    for (rowOffset in -1..1) {
                        for (colOffset in -1..1) {
                            val newRow = i + rowOffset
                            val newCol = j + colOffset

                            // Check if the new position is within bounds and not occupied by a black piece
                            if (newRow in 0 until 8 && newCol in 0 until 8 && !currentBoardState.piecesState[newRow][newCol].contains("b")) {
                                possibleMoves.add(Pair(newRow, newCol))
                            }
                        }
                    }
                }
            }

            else -> currentBoardState.possibleMoves
        }
        // Update the board state with added possibleMoves
        _chessBoardUiState.update { currentBoardState.copy(possibleMoves = possibleMoves) }
    }
}