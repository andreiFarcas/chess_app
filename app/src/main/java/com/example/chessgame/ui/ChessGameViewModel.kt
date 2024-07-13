package com.example.chessgame.ui

import android.content.Context
import android.os.Build
import kotlinx.coroutines.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chessgame.data.ChessBoardState
import com.example.chessgame.engine.ChessEngine
import com.example.chessgame.interfaces.BluetoothManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

/*

 ChessGameViewModel is responsible to react and provide information for and after all UI interactions
 during a game.

 */
@RequiresApi(Build.VERSION_CODES.O)
class ChessGameViewModel(private val chessEngine: ChessEngine, private val bluetoothManager: BluetoothManager) : ViewModel() {

    private val _chessBoardUiState = MutableStateFlow(ChessBoardState())
    val chessBoardUiState: StateFlow<ChessBoardState> = _chessBoardUiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMoveToBluetooth(fromRow: Int, fromColumn: Int, toRow: Int, toColumn: Int){
        bluetoothManager.sendDataToDevice("$fromRow$fromColumn$toRow$toColumn\n")
    }

    fun checkPlayVsStockfish(): Boolean{
        return _chessBoardUiState.value.playVsStockfish
    }
    fun setPlayVsStockfish(boolValue: Boolean) {
        val currentState = _chessBoardUiState.value
        if(currentState.playVsStockfish != boolValue){
            _chessBoardUiState.value = currentState.copy(playVsStockfish = boolValue)
        }
        if(boolValue){
            bluetoothManager.sendDataToDevice("s\n") // resstarts the board state and places Arduino intor turn = 1
        }
    }

    fun testFenInterface(): String {
        val currentBoardState = _chessBoardUiState.value

        return chessEngine.getFenFromChessBoardState(currentBoardState)
    }

    private val _bestMoveText = MutableStateFlow("Waiting for Stockfish\n...")
    val bestMoveText: StateFlow<String> = _bestMoveText.asStateFlow() // Will be read in practice mode

    // Launches the chessEngine.getBestMove in a separate thread to avoid blocking the main thread
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
                "Medium" -> currentState.copy(difficultyStockfish = 2)
                "Hard" -> currentState.copy(difficultyStockfish = 3)
                "Professional" -> currentState.copy(difficultyStockfish = 5)
                else -> currentState // No change if the difficulty string doesn't match
            }
        }
    }

    fun moveByStockfish() {
        viewModelScope.launch {
            // Perform the move calculation on a background thread
            withContext(Dispatchers.IO) {
                // Get the current board state and FEN representation
                val currentBoardState = _chessBoardUiState.value
                val fen = chessEngine.getFenFromChessBoardState(currentBoardState)
                Log.d("BluetoothDebug", "moveByStockfish called")

                // Perform the move calculation directly (this will block the main thread)
                val bestMove: String? = chessEngine.getBestMove(fen, currentBoardState.difficultyStockfish)
                Log.d("BluetoothDebug", "bestMove computed")

                bestMove?.let {
                    // Convert from letter representation to normal square indexes
                    val initialSquare = Pair(8 - it[1].digitToInt(), letterToNumber(it[0]))
                    val targetSquare = Pair(8 - it[3].digitToInt(), letterToNumber(it[2]))

                    // Also send Stockfish moves to Bluetooth
                    sendMoveToBluetooth(initialSquare.first, initialSquare.second, targetSquare.first, targetSquare.second)
                    Log.d("BluetoothDebug", "Move sent to bluetooth")

                    // Update the UI directly (this will also block the main thread)
                    movePiece(initialSquare.first, initialSquare.second, targetSquare.first, targetSquare.second)
                    Log.d("BluetoothDebug", "UI updated")
                }
            }
        }

    }

    // Used to covert a move like e2e4 to regular indexes
    private fun letterToNumber(letter: Char): Int {
        return letter.code - 'a'.code  // basically maps a->7, b->6, c->5 etc...
    }

    // Converts coordinates to normal chess representation
    fun moveConversion(moveCoordinates: List<Int>, moveNumber: Int):String{
        val fromRow = moveCoordinates[0]
        val fromCol = moveCoordinates[1]
        val toRow = moveCoordinates[2]
        val toCol = moveCoordinates[3]

        // Convert the column index to letter ('a' to 'h')
        val columnLetters = "abcdefgh"

        val fromColLetter = columnLetters[fromCol]
        val fromRowNumber = 8 - fromRow
        val toColLetter = columnLetters[toCol]
        val toRowNumber = 8 - toRow

        return "$moveNumber: $fromColLetter$fromRowNumber$toColLetter$toRowNumber"
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
        // Send return home to the board
        bluetoothManager.sendDataToDevice("s\n")
    }

    // Function that moves a piece from starting position to target position
    @RequiresApi(Build.VERSION_CODES.O)
    private fun movePiece(fromRow: Int, fromColumn: Int, toRow: Int, toColumn: Int) {

        val currentBoardState = _chessBoardUiState.value
        val pieceToMove = currentBoardState.piecesState[fromRow][fromColumn] // String code of piece to move

        // Create a new pieces state table in a copy
        val newPiecesState = mutableListOf<MutableList<String>>()
        for (row in currentBoardState.piecesState) {
            newPiecesState.add(row.toMutableList())
        }

        // Move the piece to destination position
        newPiecesState[toRow][toColumn] = pieceToMove
        newPiecesState[fromRow][fromColumn] = ""

        // Check if the move is castling
        if ((pieceToMove == "wK" || pieceToMove == "bK") && (fromColumn == 4) && (fromRow == 0 || fromRow == 7)) {
            if (fromColumn - toColumn == -2) {    // King Side castle
                // move rook from column 7 to column 5
                val rookToMove = newPiecesState[fromRow][7]
                newPiecesState[fromRow][5] = rookToMove
                newPiecesState[fromRow][7] = ""
            }
            if (fromColumn - toColumn == 2) {    // Queen Side castle
                // move rook from column 0 to column 3
                val rookToMove = newPiecesState[fromRow][0]
                newPiecesState[fromRow][3] = rookToMove
                newPiecesState[fromRow][0] = ""
            }
        }

        val afterMoveBoardState = currentBoardState.copy(
            playVsStockfish = currentBoardState.playVsStockfish,
            piecesState = newPiecesState,
            whiteTurn = currentBoardState.whiteTurn,
            bKingInCheck = currentBoardState.bKingInCheck
        )

        // PIECE OF CODE THAT CHECKS FOR CHECK - NOT FUNCTIONAL
/*        var bKingInCheck = false
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
        }*/

        // Increment the move counter only after black moves (for FEN)
        var moveNumber:Int = currentBoardState.moveCounter
        if(!currentBoardState.whiteTurn)
            moveNumber += 1

        // Update the flags for castling
        var wKMoved = currentBoardState.wKMoved
        var bKMoved = currentBoardState.bKMoved
        var wKRookMoved = currentBoardState.wKRookMoved
        var bKRookMoved = currentBoardState.bKRookMoved
        var wQRookMoved = currentBoardState.wQRookMoved
        var bQRookMoved = currentBoardState.bQRookMoved

        when (pieceToMove) {
            "wK" -> wKMoved = true
            "bK" -> bKMoved = true
            "wR" -> {
                if (fromRow == 0 && fromColumn == 0) wQRookMoved = true // Queen-side rook
                if (fromRow == 0 && fromColumn == 7) wKRookMoved = true // King-side rook
            }
            "bR" -> {
                if (fromRow == 7 && fromColumn == 0) bQRookMoved = true // Queen-side rook
                if (fromRow == 7 && fromColumn == 7) bKRookMoved = true // King-side rook
            }
        }

        val newBoardState = currentBoardState.copy(
            playVsStockfish = currentBoardState.playVsStockfish,
            piecesState = newPiecesState,
            whiteTurn = !currentBoardState.whiteTurn,
            //bKingInCheck = bKingInCheck,
            moveCounter = moveNumber,
            wKMoved = wKMoved,
            bKMoved = bKMoved,
            wKRookMoved = wKRookMoved,
            bKRookMoved = bKRookMoved,
            wQRookMoved = wQRookMoved,
            bQRookMoved = bQRookMoved
        )

        // Update the ChessBoardState with the new board state
        _chessBoardUiState.value = newBoardState

        // If we are just watching a recorded game don't do anything
        if(!newBoardState.watchRecording) {

            // If we play vs CPU and is not our turn let CPU move
            if (!newBoardState.whiteTurn && newBoardState.playVsStockfish)
                moveByStockfish()

            // If we play vs CPU and is white turn wait for move by human:
            if (newBoardState.whiteTurn && newBoardState.playVsStockfish)
                moveFromChessboard()

            // If we are recording a game, always wait for move from chessboard:
            Log.d("Recording", "recording game flag: ${newBoardState.recordingGame}")
            if (newBoardState.recordingGame)
                moveFromChessboard()

            // If we are in practice mode, recompute the suggested move every time:
            if (!newBoardState.playVsStockfish && !newBoardState.recordingGame) {
                findBestMoveByStockfish()
            }
        }
    }

    // Updates the gameRecording status
    fun startRecording(){
        bluetoothManager.sendDataToDevice("s\n")
        _chessBoardUiState.update { ChessBoardState(playVsStockfish = false) }   // Rebuilds the board state
        val currentBoardState = _chessBoardUiState.value
        // Send start Recording Mode to the board
        _chessBoardUiState.value = currentBoardState.copy(recordingGame = true, moves = mutableListOf())

        Log.d("Recording", "Start Recording sent to chessboard and recordingGame flag set ${_chessBoardUiState.value.recordingGame}")
    }

    // Saves recording to a file
    fun saveRecording(context: Context, recordingName: String){
        val currentBoardState = _chessBoardUiState.value

        saveMovesToFile(context, recordingName) // Saves all moves to the specified file

        _chessBoardUiState.value = currentBoardState.copy(recordingGame = false, moves = mutableListOf())
        resetBoard()
    }

    // Resets board and recording status
    fun resetRecording(){
        val currentBoardState = _chessBoardUiState.value
        _chessBoardUiState.value = currentBoardState.copy(recordingGame = false, moves = mutableListOf())

        resetBoard()
    }

    // Function that waits for data to be recieved from chessboard and executes the move
    fun moveFromChessboard(){
        Log.d("bluetooth", "moveFromChessboard() called")

        val receivedMove = bluetoothManager.receiveDataFromDevice()

        Log.d("bluetooth", "Recived move: $receivedMove")

        // Data received in format (fromRow, fromColumn, toRow, toColumn)
        val trimmedMessage = receivedMove.trim().split(" ")

        if (trimmedMessage.size == 4) {
            val fromRow = trimmedMessage[0].toInt()
            val fromColumn = trimmedMessage[1].toInt() - 2
            val toRow = trimmedMessage[2].toInt()
            val toColumn = trimmedMessage[3].toInt() - 2

            // If we are recording the game, save the move in the temporary list
            val currentBoardState = _chessBoardUiState.value

            Log.d("Record", "current flag: ${currentBoardState.recordingGame}")

            if(currentBoardState.recordingGame == false){
                // Try to filter false readings if it asks to move a piece from a empty position
                if(currentBoardState.piecesState[fromRow][fromColumn] != "" && (fromColumn in 0..7))
                    movePiece(fromRow, fromColumn, toRow, toColumn)
                else
                    moveFromChessboard()
            } else{    // we are recording
                // Create a copy of the current moves list and add the new move
                val updatedMoves = currentBoardState.moves.toMutableList()

                if(toColumn < 8 && toColumn > -1){     // Move happened on the chessboard (8x8)
                    val move = listOf<Int>(fromRow, fromColumn, toRow, toColumn)

                    updatedMoves.add(move)
                    Log.d("Record", "Move ${currentBoardState.currentMove + 1} added: $fromRow-$fromColumn to $toRow-$toColumn")

                    // Confirm to chessboard we are still recording:
                    bluetoothManager.sendDataToDevice("r\n")
                    Log.d("Recording", "r sent to bluetooth")

                    // Update the UI state with the new moves list
                    _chessBoardUiState.value = currentBoardState.copy(moves = updatedMoves, currentMove = currentBoardState.currentMove + 1)
                    movePiece(fromRow, fromColumn, toRow, toColumn)

                } else{     // Piece was moved outside the 8x8
                    val capturedPiece = currentBoardState.piecesState[fromRow][fromColumn]
                    val captureMoveIndex = currentBoardState.currentMove + 1 // Capture is part of last executed move + 1
                    val capture = Pair<Int, String>(captureMoveIndex, capturedPiece)

                    val updatedCaptures = currentBoardState.captures.toMutableList()
                    updatedCaptures.add(capture)

                    Log.d("Record", "Capture of piece added at move: ${captureMoveIndex}, for piece ${capturedPiece}")
                    // Update the UI state with the captured Piece
                    _chessBoardUiState.value = currentBoardState.copy(captures = updatedCaptures)

                    // Confirm to chessboard we are still recording:
                    bluetoothManager.sendDataToDevice("r\n")
                    Log.d("Recording", "r sent to bluetooth")

                    movePiece(fromRow, fromColumn, toRow, toColumn)
                }
            }
        }
    }

    // Executes current move
    fun moveForward(){
        val currentBoardState = _chessBoardUiState.value
        val currentMove = currentBoardState.currentMove
        val movesList = currentBoardState.moves

        // Execute a forward move
        if (currentMove < movesList.size) {
            val toExecute = movesList[currentMove]
            Log.d("movesNavigation", "Attempting move ${currentMove + 1}: ${toExecute[0]}, ${toExecute[1]}, ${toExecute[2]}, ${toExecute[3]}")
            movePiece(toExecute[0], toExecute[1], toExecute[2], toExecute[3])
            _chessBoardUiState.value = _chessBoardUiState.value.copy(currentMove = currentBoardState.currentMove + 1)
        }
    }

    // Executes previous move
    fun moveBack() {
        val currentBoardState = _chessBoardUiState.value
        val currentMove = currentBoardState.currentMove
        val movesList = currentBoardState.moves
        val captures = currentBoardState.captures

        if (currentMove > 0) {
            // Execute the reverse of the move
            val toExecute = movesList[currentMove - 1]
            Log.d("movesNavigation", "Attempting move ${currentMove}: ${toExecute[0]}, ${toExecute[1]}, ${toExecute[2]}, ${toExecute[3]}")
            movePiece(toExecute[2], toExecute[3], toExecute[0], toExecute[1])


            val newPiecesState = _chessBoardUiState.value.piecesState.map { it.toMutableList() }.toMutableList() // Mutable copy of piecesState
            // Check if we had a capture in this move so we "revive" the piece that was captured
            val capture = captures.find { it.first == currentMove }
            capture?.let {
                val capturedPiece = it.second
                // add to the copy of piecesState matrix the revived piece
                newPiecesState[toExecute[2]][toExecute[3]] = capturedPiece
            }

            // Update the chess board state
            _chessBoardUiState.value = _chessBoardUiState.value.copy(
                currentMove = currentMove - 1,
                piecesState = newPiecesState
            )
        }
    }

    // Function used to save moves
    fun saveMovesToFile(context: Context, filename: String) {
        // Define the folder for recordings
        val recordingsFolder = File(context.filesDir, "recordings")

        // Create the folder if it doesn't exist - for first time saving only
        if (!recordingsFolder.exists()) {
            recordingsFolder.mkdir()
        }

        // Save all moves and captures from ChessBoardState to a file
        val filename = "$filename.txt"
        val file = File(recordingsFolder, filename)
        file.printWriter().use { out ->
            _chessBoardUiState.value.moves.forEach { move ->
                out.println(move.joinToString(","))
                Log.d("Recording", "Move Saved to $filename: ${move.joinToString(",")}")
            }
            // Write a separator line
            out.println("===")
            Log.d("Recording", "Separator Saved to $filename")
            _chessBoardUiState.value.captures.forEach { capture ->
                out.println("${capture.first},${capture.second}")
                Log.d("Recording", "Capture Saved to $filename: ${capture.first},${capture.second}")
            }
        }
    }

    fun getAllRecordingFiles(context: Context): List<File> {
        val recordingsFolder = File(context.filesDir, "recordings")
        if (!recordingsFolder.exists()) {
            return emptyList()
        }
        return recordingsFolder.listFiles()?.toList() ?: emptyList()
    }

    // Function to load moves from a file
    fun loadMovesFromFile(filename: String) {
        Log.d("FileLoad", "loadMovesFromFile called for file: $filename")
        val file = File(filename)

        if (file.exists()) {
            Log.d("FileLoad", "File Exists")

            // Read and log all lines for debug purposes
            val fileContent = file.readText()
            Log.d("FileLoad", "File Content:\n$fileContent")

            val lines = file.readLines()
            val separatorIndex = lines.indexOf("===")

            // Parse moves
            val loadedMoves = lines.take(separatorIndex).map { line ->
                line.split(",").map { it.toInt() }
            }

            // Parse captures
            val loadedCaptures = lines.drop(separatorIndex + 1).map { line ->
                val (moveIndex, piece) = line.split(",")
                moveIndex.toInt() to piece
            }

            // Logs for debug only
            loadedMoves.forEachIndexed { index, move ->
                val (fromRow, fromColumn, toRow, toColumn) = move
                Log.d("FileLoad", "Move ${index + 1}: $fromRow-$fromColumn to $toRow-$toColumn")
            }

            loadedCaptures.forEachIndexed { index, capture ->
                val (moveIndex, piece) = capture
                Log.d("FileLoad", "Capture ${index + 1}: MoveIndex=$moveIndex, Piece=$piece")
            }

            // Reset board
            resetBoard()

            // Update ChessBoardState with the moves and captures list
            _chessBoardUiState.value = _chessBoardUiState.value.copy(
                moves = loadedMoves.toMutableList(),
                captures = loadedCaptures.toMutableList(),
                watchRecording = true
            )
        } else {
            Log.d("FileLoad", "File does not exist")
        }
    }

    fun deleteFile(fileName: String):Boolean {
        val file = File(fileName)
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
                // Check for castling possibilities if the king has not moved
                if (!currentBoardState.wKMoved) {
                    // Check king-side castling
                    if (!currentBoardState.wKRookMoved) {
                        // Check if squares between king and rook are empty and not under attack
                        if (currentBoardState.piecesState[7][6] == "" && currentBoardState.piecesState[7][5] == "") {
                            possibleMoves.add(Pair(7, 6))  // King-side castling move
                        }
                    }

                    // Check queen-side castling
                    if (!currentBoardState.bQRookMoved) {
                        // Check if squares between king and rook are empty and not under attack
                        if (currentBoardState.piecesState[7][1] == "" && currentBoardState.piecesState[7][2] == "" && currentBoardState.piecesState[7][3] == "") {
                            possibleMoves.add(Pair(7, 2))  // Queen-side castling move
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

                // Check for castling possibilities if the king has not moved
                if (!currentBoardState.bKMoved) {
                    // Check king-side castling
                    if (!currentBoardState.bKRookMoved) {
                        // Check if squares between king and rook are empty and not under attack
                        if (currentBoardState.piecesState[0][6] == "" && currentBoardState.piecesState[0][5] == "") {
                            possibleMoves.add(Pair(0, 6))  // King-side castling move
                        }
                    }

                    // Check queen-side castling
                    if (!currentBoardState.bQRookMoved) {
                        // Check if squares between king and rook are empty and not under attack
                        if (currentBoardState.piecesState[0][1] == "" && currentBoardState.piecesState[0][2] == "" && currentBoardState.piecesState[0][3] == "") {
                            possibleMoves.add(Pair(0, 2))  // Queen-side castling move
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