package jp.co.zeppelin.nec.hearable.domain.model

import android.bluetooth.BluetoothDevice

/**
 * Sealed class representing various possible NEC Hearable SDK responses
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class BluetoothMsgBase

/**
 * Bluetooth device found via scan; confirms device turned on and discoverable
 */
data class BluetoothTargetDeviceFound(
    val device: BluetoothDevice
) : BluetoothMsgBase()
