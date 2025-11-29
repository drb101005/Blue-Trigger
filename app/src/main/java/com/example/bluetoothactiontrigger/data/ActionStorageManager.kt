package com.example.bluetoothactiontrigger.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActionStorageManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bluetooth_actions", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val key = "device_actions"

    // ✅ Load all stored device-action mappings
    fun getAllActions(): MutableList<DeviceAction> {
        val json = prefs.getString(key, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<DeviceAction>>() {}.type
        return gson.fromJson(json, type)
    }

    // ✅ Save the full list
    private fun saveAll(list: MutableList<DeviceAction>) {
        val json = gson.toJson(list)
        prefs.edit().putString(key, json).apply()
    }

    // ✅ Get action for a specific device
    fun getActionForDevice(address: String): DeviceAction? {
        return getAllActions().find { it.deviceAddress == address }
    }

    // ✅ Set or update actions for a device
    fun setActionForDevice(deviceAction: DeviceAction) {
        val all = getAllActions()
        val existing = all.find { it.deviceAddress == deviceAction.deviceAddress }
        if (existing != null) {
            existing.onConnectAction = deviceAction.onConnectAction
            existing.onDisconnectAction = deviceAction.onDisconnectAction
        } else {
            all.add(deviceAction)
        }
        saveAll(all)
    }

    // ✅ Optional: Clear all mappings
    fun clearAll() {
        prefs.edit().remove(key).apply()
    }
}
