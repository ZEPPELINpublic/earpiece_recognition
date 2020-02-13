package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

sealed class BluetoothAclConnectBase

/**
 * Repr for Bluetooth ACL connect status, emitted by B/C receiver onReceive() callback
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class BluetoothAclConnected(
    val macAddress: String
) : BluetoothAclConnectBase()

object BluetoothAclDisonnectRequested : BluetoothAclConnectBase()
object BluetoothAclDisonnected : BluetoothAclConnectBase()
