package com.example.bluetoothactiontrigger.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.bluetoothactiontrigger.services.BluetoothForegroundService

class BluetoothRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BluetoothRestartReceiver", "Restarting BluetoothForegroundService")
        val serviceIntent = Intent(context, BluetoothForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
