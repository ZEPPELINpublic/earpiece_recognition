package jp.co.zeppelin.nec.hearable.domain.model

import android.bluetooth.BluetoothDevice

/**
 * Sealed class representing possible bluetooth device paired/connected statuses
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class BluetoothConnectStatusBase(
    val targetMacAddr: String,
    val device: BluetoothDevice?
)

class PairedAndConnected(
    targetMacAddr: String,
    device: BluetoothDevice?
) : BluetoothConnectStatusBase(targetMacAddr, device)

class NotPairedAndConnected(
    targetMacAddr: String,
    device: BluetoothDevice?
) : BluetoothConnectStatusBase(targetMacAddr, device)
