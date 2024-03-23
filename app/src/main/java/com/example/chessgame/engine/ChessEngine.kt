package com.example.chessgame.engine
import android.content.Context
import android.util.Log
import com.example.chessgame.data.ChessBoardState
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/*

Class that creates an instance of stockfish and provides functions that facilitate communication
between the chess engine and our app.

 */
class ChessEngine(context: Context) {
    private var process: Process? = null

    init {
        // Obtain the path to the Stockfish executable within the application's internal storage
        val stockfishPath = File(context.filesDir, "stockfish").absolutePath
        // Start Stockfish process
        process = Runtime.getRuntime().exec(stockfishPath)
    }

    fun getBestMove(fen: String, difficulty: Int): String? {
        val outputStream = OutputStreamWriter(process!!.outputStream)
        val inputStream = BufferedReader(InputStreamReader(process!!.inputStream))

        outputStream.write("setoption name Skill Level value $difficulty\n")
        outputStream.write("position fen $fen\n")
        outputStream.write("go movetime 2000\n")
        outputStream.flush() // make sure we send the commands immediately

        var line: String?
        var bestMove: String? = null
        while (inputStream.readLine().also { line = it } != null) {
            line?.let { Log.d("Stockfish Output", it) } // Log everything Stockfish outputs
            // If line starts with "bestmove" extract the response
            if (line?.startsWith("bestmove") == true) {
                bestMove = line!!.split(" ")[1]
                break
            }
        }

        return bestMove
    }

    fun close() {
        process?.destroy()
    }

    // Interface between our position storage and FEN (used by Stockfish)
    fun getFenFromChessBoardState(boardState: ChessBoardState): String {
        val fenStringBuilder = StringBuilder()
        boardState.piecesState.forEach { row ->
            var emptySquares = 0
            row.forEach { square ->
                if (square.isEmpty()) {
                    emptySquares++
                } else {
                    if (emptySquares > 0) {
                        fenStringBuilder.append(emptySquares) // we found a piece and append the fen
                        emptySquares = 0
                    }
                    // Convert our notation to FEN notation
                    if(square[0] == 'b')
                        // FEN black pieces are lowercase
                        fenStringBuilder.append(square[1].lowercaseChar())
                    else
                        // FEN white pieces remain uppercase
                        fenStringBuilder.append(square[1])
                }
            }
            if (emptySquares > 0) {
                fenStringBuilder.append(emptySquares)
            }
            fenStringBuilder.append('/')
        }
        fenStringBuilder.removeSuffix("/")

        // Add turn black/white
        if(boardState.whiteTurn)
            fenStringBuilder.append(" w")
        else
            fenStringBuilder.append(" b")

        // We have to add white and black castling possibility, en passant and move counter
        fenStringBuilder.append(" KQkq - 0 ${boardState.moveCounter}")

        return fenStringBuilder.toString()
    }
}
