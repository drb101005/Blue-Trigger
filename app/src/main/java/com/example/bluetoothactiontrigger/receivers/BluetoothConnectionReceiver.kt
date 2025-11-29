package com.example.bluetoothactiontrigger.receivers

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.bluetoothactiontrigger.core.ActionExecutor
import com.example.bluetoothactiontrigger.data.ActionStorageManager

class BluetoothConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return

        val storageManager = ActionStorageManager(context)
        val executor = ActionExecutor(context)

        val deviceAction = storageManager.getActionForDevice(device.address)

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Toast.makeText(context, "Connected: ${device.name}", Toast.LENGTH_SHORT).show()
                deviceAction?.onConnectAction?.let {
                    executor.execute(it)
                }
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                Toast.makeText(context, "Disconnected: ${device.name}", Toast.LENGTH_SHORT).show()
                deviceAction?.onDisconnectAction?.let {
                    executor.execute(it)
                }
            }
        }
    }
}
