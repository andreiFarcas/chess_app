package com.example.chessgame

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chessgame.engine.ChessEngine
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.theme.ChessGameTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    // Initialise the chess engine in the viewModel
    private lateinit var viewModel: ChessGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyStockfishToInternalStorage()

        // Create the ChessEngine with applicationContext
        val chessEngine = ChessEngine(applicationContext)

        // Directly instantiate the ChessGameViewModel
        viewModel = ChessGameViewModel(chessEngine)

        setContent {
            ChessGameTheme {
                ChessGameApp(viewModel)
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
        val bestMove = chessEngine.getBestMove(fen, 15)
        Log.d("Stockfish", "Best move: $bestMove")
        chessEngine.close()
    }

}
