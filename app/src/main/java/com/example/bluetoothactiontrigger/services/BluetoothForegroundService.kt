package com.example.bluetoothactiontrigger.services
import com.example.bluetoothactiontrigger.receivers.BluetoothRestartReceiver

import android.app.*
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bluetoothactiontrigger.R
import com.example.bluetoothactiontrigger.receivers.BluetoothConnectionReceiver

class BluetoothForegroundService : Service() {

    private val receiver = BluetoothConnectionReceiver()
    private val channelId = "bluetooth_action_service"

    override fun onCreate() {
        super.onCreate()
        Log.d("BluetoothService", "Service created")

        // Register Bluetooth connect/disconnect
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(receiver, filter)

        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Bluetooth Action Service")
            .setContentText("Monitoring Bluetooth connections")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BluetoothService", "Service onStartCommand called")
        scheduleRestart(this)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        Log.d("BluetoothService", "Service destroyed – scheduling restart")
        scheduleRestart(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bluetooth Action Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun scheduleRestart(context: Context) {
        try {
            val intent = Intent(context, BluetoothRestartReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerAt = System.currentTimeMillis() + 30_000 // 30 seconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
            Log.d("BluetoothService", "Restart scheduled in 30 s")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
