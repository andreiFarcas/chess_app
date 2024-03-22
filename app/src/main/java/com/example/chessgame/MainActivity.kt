package com.example.chessgame

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chessgame.engine.ChessEngine
import com.example.chessgame.ui.theme.ChessGameTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyStockfishToInternalStorage()

        setContent {
            ChessGameTheme {
                ChessGameApp()
            }
        }
    }

    // Function used to copy the executable stockfish file in the internal memory of the device
    //      internal memory refers to a part of memory that is allocated to our app by Android and
    //      it allows us to store and execute files like the stockfish compiled binary.
    private fun copyStockfishToInternalStorage() {
        val inputStream = assets.open("stockfish")
        val outFile = File(filesDir, "stockfish")
        val outputStream = FileOutputStream(outFile)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()

        // Set executable permissions
        outFile.setExecutable(true)
    }

    private fun chessEngineTest(){
        val chessEngine = ChessEngine(this)
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\n" // Example FEN, replace with current position
        val bestMove = chessEngine.getBestMove(fen)
        Log.d("Stockfish", "Best move: $bestMove")
        chessEngine.close()
    }

}
