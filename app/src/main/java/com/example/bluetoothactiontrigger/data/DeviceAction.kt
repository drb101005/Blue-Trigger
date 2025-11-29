package com.example.bluetoothactiontrigger.data

data class DeviceAction(
    val deviceAddress: String,
    var onConnectAction: ActionType = ActionType.NONE,
    var onDisconnectAction: ActionType = ActionType.NONE
)
