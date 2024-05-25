package com.example.chessgame.interfaces

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Context.COMPANION_DEVICE_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat.getSystemService
import com.example.chessgame.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException
import java.util.UUID
import java.util.regex.Pattern

@RequiresApi(Build.VERSION_CODES.O)
class BluetoothManager(private val activity: ComponentActivity) {

    private var connectedSocket: BluetoothSocket? = null

    // Companion Device Manager provides the methods used to connect to bluetooth devices
    private val deviceManager: CompanionDeviceManager by lazy {
        activity.getSystemService(COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }

    // MutableStateFlow that tracks connection status
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus = _connectionStatus.asStateFlow()

    // Checks to see if a board is connected or not
    private fun changeConnectionStatus(connection: Boolean) {
        _connectionStatus.value = connection
    }

    fun checkPermissions() {
        // Check if the BLUETOOTH_CONNECT permission is already granted (Obs. only needed from Android 12)
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If not, request it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    MainActivity.REQUEST_BLUETOOTH_CONNECT_PERMISSION
                )
            }
            Log.d("BluetoothDebug", "Permission requested")
        }
    }

    // Called from MainActivity once the user selects a device, connects to it
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.SELECT_DEVICE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val device: BluetoothDevice? =
                data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)

            if (device != null) {
                connectDevice(device)  // Connect to the selected device
            }
        } else {
            Log.d(
                "BluetoothDebug",
                "Result not OK or request code does not match: requestCode = $requestCode, resultCode = $resultCode"
            )
        }
    }

    // Searches and allows the user to select a bluetooth device named HC-05 available to connect
    fun searchForDevices() {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .setNamePattern(Pattern.compile("HC-05"))
            .build()

        val pairingRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(false)
            .build()

        deviceManager.associate(pairingRequest,
            object : CompanionDeviceManager.Callback() {
                @Deprecated("Deprecated in Java")
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    activity.startIntentSenderForResult(
                        chooserLauncher,
                        MainActivity.SELECT_DEVICE_REQUEST_CODE,
                        null,
                        0,
                        0,
                        0
                    )
                }

                override fun onFailure(error: CharSequence?) {
                    Log.e("BluetoothDebug", "Pairing failed: $error")
                }
            }, null
        )
    }

    // Connects the bluetooth device
    fun connectDevice(device: BluetoothDevice) {
        Thread {
            try {
                Log.d("BluetoothDebug", "Attempting to connect to the device")
                val socket: BluetoothSocket =
                    device.createRfcommSocketToServiceRecord(MainActivity.HC05_UUID)

                // On Android 12 and above, check for BLUETOOTH_CONNECT permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(
                        "BluetoothDebug",
                        "BLUETOOTH_CONNECT permission not granted on Android 12+ device."
                    )
                    // Handle permission not granted scenario appropriately. For Android 9, this block will not execute.
                } else {
                    // Proceed with Bluetooth connection as either the permission is granted or not required (on Android 9 and below).
                    socket.connect() // This can throw an IOException
                    connectedSocket = socket
                    changeConnectionStatus(true)
                    Log.d("BluetoothDebug", "Connection established")
                }
            } catch (e: IOException) {
                Log.e("BluetoothDebug", "Connection failed: " + e.message, e)
            }
        }.start()
    }

    // Disconnects from the device
    fun disconnectDevice() {
        connectedSocket?.close()
        changeConnectionStatus(false) // Eventually lets the UI know we disconnected
    }

    // Can send a string to the connected chessboard
    fun sendDataToDevice(dataString: String) {
        Thread {
            try {
                val outputStream = connectedSocket?.outputStream
                outputStream?.let {
                    it.write(dataString.toByteArray())
                    Log.d("BluetoothDebug", "Data sent: $dataString")
                } ?: run {
                    Log.e("BluetoothDebug", "Error: BluetoothSocket is not connected.")
                }
            } catch (e: IOException) {
                Log.e("BluetoothDebug", "Failed to send data: " + e.message, e)
            }
        }.start()
    }

    // Receives data from chessboard
    fun receiveDataFromDevice(): String {
        var incomingMessage = ""
        try {
            val inputStream = connectedSocket?.inputStream
            val buffer = ByteArray(1024)
            val stringBuilder = StringBuilder()

            if (connectedSocket == null || !connectedSocket!!.isConnected) {
                Log.d("BluetoothDebug", "Aci ii baiu")
            }

            // Keep listening to the InputStream until a \n character is received
            while (connectedSocket != null) {
                val bytes = inputStream?.read(buffer) ?: -1
                if (bytes > 0) {
                    val incomingPart = String(buffer, 0, bytes)
                    stringBuilder.append(incomingPart)

                    // Check if the incoming message contains a newline character
                    val tempMessage = stringBuilder.toString()
                    val newlineIndex = tempMessage.indexOf('\n')
                    if (newlineIndex != -1) {
                        // Extract the complete message up to the newline character
                        incomingMessage = tempMessage.substring(0, newlineIndex).trim()
                        Log.d("BluetoothDebug", "Received data: $incomingMessage")

                        // Remove the processed part from the StringBuilder
                        stringBuilder.delete(0, newlineIndex + 1)
                        break
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("BluetoothDebug", "Error occurred when receiving data: " + e.message, e)
        }
        return incomingMessage
    }
}





