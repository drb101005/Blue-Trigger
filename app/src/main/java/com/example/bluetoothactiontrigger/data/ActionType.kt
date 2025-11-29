package com.example.bluetoothactiontrigger.data

enum class ActionType(val displayName: String) {
    LOCK_PHONE("Lock Phone"),
    OPEN_CAMERA("Open Camera"),
    TOGGLE_FLASHLIGHT("Toggle Flashlight"),
    FLASHLIGHT_ON("Flashlight On"),
    FLASHLIGHT_OFF("Flashlight Off"),
    NONE("No Action");

    companion object {
        fun fromDisplayName(name: String): ActionType {
            return values().find { it.displayName == name } ?: NONE
        }
    }
}
