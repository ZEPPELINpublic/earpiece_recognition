package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

/**
 * Repr for BLE connect status, emitted in BluetoothGattCallback::onConnectionStateChange() listener
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class BluetoothLeConnectBase

object BluetoothLeConnected : BluetoothLeConnectBase()

object BluetoothLeDisconnected : BluetoothLeConnectBase()
