package com.example.bluetoothactiontrigger.core

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.PowerManager
import android.provider.MediaStore
import android.widget.Toast
import com.example.bluetoothactiontrigger.data.ActionType
import com.example.bluetoothactiontrigger.receivers.DeviceAdminReceiver

class ActionExecutor(private val context: Context) {

    private var flashlightOn = false

    fun execute(action: ActionType) {
        when (action) {
            ActionType.LOCK_PHONE -> lockPhone()
            ActionType.OPEN_CAMERA -> openCamera()
            ActionType.TOGGLE_FLASHLIGHT -> setFlashlight(!flashlightOn)
            ActionType.FLASHLIGHT_ON -> setFlashlight(true)
            ActionType.FLASHLIGHT_OFF -> setFlashlight(false)
            else -> Toast.makeText(context, "No action assigned", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔒 Locks the phone (requires Device Admin)
    private fun lockPhone() {
        try {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val compName = ComponentName(context, DeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(compName)) {
                dpm.lockNow()
                Toast.makeText(context, "Phone locked", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Grant Device Admin permission first", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error locking phone", Toast.LENGTH_SHORT).show()
        }
    }

    // 📷 Opens the default camera app
    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Opening camera...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error opening camera", Toast.LENGTH_SHORT).show()
        }
    }

    // 💡 Toggles flashlight (no root or ADB required)
    private fun setFlashlight(state: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.first()
            cameraManager.setTorchMode(cameraId, state)
            flashlightOn = state
            val msg = if (state) "Flashlight ON" else "Flashlight OFF"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
