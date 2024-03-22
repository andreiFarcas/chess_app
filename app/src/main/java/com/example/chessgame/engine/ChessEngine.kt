package com.example.chessgame.engine
import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class ChessEngine(context: Context) {
    private var process: Process? = null

    init {
        val stockfishPath = File(context.filesDir, "stockfish").absolutePath
        process = Runtime.getRuntime().exec(stockfishPath)
    }

    fun getBestMove(fen: String): String? {
        val outputStream = OutputStreamWriter(process!!.outputStream)
        val inputStream = BufferedReader(InputStreamReader(process!!.inputStream))

        outputStream.write("position fen $fen\n")
        outputStream.write("go movetime 2000\n") // Adjust as needed
        outputStream.flush()

        var line: String?
        var bestMove: String? = null
        while (inputStream.readLine().also { line = it } != null) {
            line?.let { Log.d("Stockfish Output", it) } // Log everything Stockfish outputs
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
}
