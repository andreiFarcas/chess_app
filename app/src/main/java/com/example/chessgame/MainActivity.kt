package com.example.chessgame

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.chessgame.engine.ChessEngine
import com.example.chessgame.interfaces.BluetoothManager
import com.example.chessgame.ui.ChessGameViewModel
import com.example.chessgame.ui.screens.ChessGameApp
import com.example.chessgame.ui.theme.ChessGameTheme
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {

    // Initialise the chess game in the viewModel
    private lateinit var viewModel: ChessGameViewModel

    // Bluetooth Manager used to connect to the board
    private val bluetoothManager = BluetoothManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyStockfishToInternalStorage()

        // Checks bluetooth permissions and requests them if they are not given
        bluetoothManager.checkPermissions()

        // Create the ChessEngine with applicationContext
        val chessEngine = ChessEngine(applicationContext)

        // Instantiate the ChessGameViewModel with the engine included
        viewModel = ChessGameViewModel(chessEngine)

        setContent {
            ChessGameTheme {
                ChessGameApp(viewModel, bluetoothManager)
            }
        }
    }

    /*
        Function used to copy the executable stockfish file in the internal memory of the device
          internal memory refers to a part of memory that is allocated to our app by Android and
          it allows us to store and execute files like the stockfish compiled binary.
    */
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

    // Companion object used to set some values needed to establish bluetooth connection
    companion object {
        const val SELECT_DEVICE_REQUEST_CODE = 1
        const val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 2
        val HC05_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    // Function called once the user selects a device from the list of bluetooth devices
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bluetoothManager.handleActivityResult(requestCode, resultCode, data)
    }

}
