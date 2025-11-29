package com.example.bluetoothactiontrigger

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothactiontrigger.core.ActionExecutor
import com.example.bluetoothactiontrigger.data.ActionStorageManager
import com.example.bluetoothactiontrigger.data.ActionType
import com.example.bluetoothactiontrigger.data.DeviceAction
import com.example.bluetoothactiontrigger.receivers.DeviceAdminReceiver
import android.content.IntentFilter
import com.example.bluetoothactiontrigger.receivers.BluetoothConnectionReceiver
import com.example.bluetoothactiontrigger.services.BluetoothForegroundService
import android.content.Context

class MainActivity : AppCompatActivity() {

    private lateinit var deviceSpinner: Spinner
    private lateinit var connectActionSpinner: Spinner
    private lateinit var disconnectActionSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var refreshButton: Button
    private lateinit var debugLockButton: Button

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var storageManager: ActionStorageManager
    private lateinit var executor: ActionExecutor

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceSpinner = findViewById(R.id.spinnerDevices)
        connectActionSpinner = findViewById(R.id.spinnerConnect)
        disconnectActionSpinner = findViewById(R.id.spinnerDisconnect)
        saveButton = findViewById(R.id.btnSave)
        debugLockButton = findViewById(R.id.btnDebugLock)
        refreshButton = findViewById(R.id.btnRefresh)

        storageManager = ActionStorageManager(this)
        executor = ActionExecutor(this)

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.CAMERA
            )
        )

        setupActionSpinners()
        setupSaveButton()
        setupDebugButtons()
        setupRefreshButton()
        initBluetooth()

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(BluetoothConnectionReceiver(), filter)
        Log.d("MainActivity", "BluetoothConnectionReceiver registered at runtime")

        val startStopButton: Button = findViewById(R.id.btnStartStopService)

        startStopButton.setOnClickListener {
            val serviceIntent = Intent(this, BluetoothForegroundService::class.java)
            if (!isServiceRunning(BluetoothForegroundService::class.java)) {
                startForegroundService(serviceIntent)
                Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
                startStopButton.text = "Stop Service"
            } else {
                stopService(serviceIntent)
                Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show()
                startStopButton.text = "Start Service"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadBluetoothDevices()
    }

    private fun initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "No Bluetooth adapter found", Toast.LENGTH_LONG).show()
        } else if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
        }
    }

    private fun loadBluetoothDevices() {
        try {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Grant Bluetooth permission", Toast.LENGTH_LONG).show()
                Log.d("MainActivity", "Permission missing: BLUETOOTH_CONNECT")
                return
            }

            val scanner = bluetoothAdapter?.bluetoothLeScanner
            try {
                val dummyCallback = object : android.bluetooth.le.ScanCallback() {}
                scanner?.startScan(dummyCallback)
                Thread.sleep(500)
                scanner?.stopScan(dummyCallback)
            } catch (_: Exception) { }

            val pairedDevices = bluetoothAdapter?.bondedDevices
            Log.d("MainActivity", "Adapter=$bluetoothAdapter Paired=${pairedDevices?.size}")

            val deviceList =
                pairedDevices?.map { "${it.name ?: "Unknown"} (${it.address})" }?.toMutableList()
                    ?: mutableListOf()

            if (deviceList.isEmpty()) deviceList.add("No paired devices found")

            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, deviceList)
            deviceSpinner.adapter = adapter

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupActionSpinners() {
        val actions = ActionType.values().map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, actions)
        connectActionSpinner.adapter = adapter
        disconnectActionSpinner.adapter = adapter
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            val selectedDevice = deviceSpinner.selectedItem?.toString() ?: return@setOnClickListener
            if (selectedDevice.startsWith("No paired")) {
                Toast.makeText(this, "Pair a device first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val address = selectedDevice.substringAfter("(").substringBefore(")")
            val connectAction =
                ActionType.fromDisplayName(connectActionSpinner.selectedItem.toString())
            val disconnectAction =
                ActionType.fromDisplayName(disconnectActionSpinner.selectedItem.toString())
            val deviceAction = DeviceAction(address, connectAction, disconnectAction)
            storageManager.setActionForDevice(deviceAction)

            Toast.makeText(
                this,
                "Actions saved for ${selectedDevice.substringBefore(" (")}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupRefreshButton() {
        refreshButton.setOnClickListener {
            Toast.makeText(this, "Refreshing devices…", Toast.LENGTH_SHORT).show()
            loadBluetoothDevices()
        }
    }

    private fun setupDebugButtons() {
        debugLockButton.setOnClickListener {
            val dpm = getSystemService(DevicePolicyManager::class.java)
            val compName = ComponentName(this, DeviceAdminReceiver::class.java)
            if (dpm != null && !dpm.isAdminActive(compName)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName as Parcelable)
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Required to lock phone on Bluetooth disconnect."
                    )
                }
                startActivity(intent)
            } else {
                executor.execute(ActionType.LOCK_PHONE)
            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) return true
        }
        return false
    }
}
