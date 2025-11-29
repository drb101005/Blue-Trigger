package com.example.bluetoothactiontrigger.receivers

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bluetoothactiontrigger.services.BluetoothForegroundService

class BluetoothStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            Log.d("BluetoothStateReceiver", "Bluetooth state changed: $state")

            if (state == BluetoothAdapter.STATE_ON) {
                // Start the service when Bluetooth turns ON
                val serviceIntent = Intent(context, BluetoothForegroundService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
                Log.d("BluetoothStateReceiver", "Bluetooth ON → service ensured running")
            }
        }
    }
}
