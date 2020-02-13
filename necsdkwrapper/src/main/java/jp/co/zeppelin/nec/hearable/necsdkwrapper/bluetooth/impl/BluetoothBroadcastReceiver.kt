package jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.impl

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract.IBluetoothContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothAclConnectBase
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothAclConnected
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothAclDisonnectRequested
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothAclDisonnected

/**
 * Simple broadcast reciever for Bluetooth "classic" connect/disconnect events
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class BluetoothBroadcastReceiver(context: Context) : BroadcastReceiver(),
    IBluetoothContract.IBcReceiverContract {
    private val TAG = BluetoothBroadcastReceiver::class.java.simpleName

    val bluetoothAclConnectedStatus_ =
        MutableLiveData<SingleLiveDataEvent<BluetoothAclConnectBase>>()
    override val bluetoothAclConnectedStatus = bluetoothAclConnectedStatus_

    init {
        // All we need to check for is fundamental "classic" bluetooth paired 'n connected state
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        context.registerReceiver(this, filter)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                var macAddr = ""
                device?.address?.let { deviceMacAddr ->
                    macAddr = deviceMacAddr
                }
                bluetoothAclConnectedStatus_.postValue(
                    SingleLiveDataEvent(
                        BluetoothAclConnected(
                            macAddress = macAddr
                        )
                    )
                )
            }
            BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                bluetoothAclConnectedStatus_.postValue(
                    SingleLiveDataEvent(
                        BluetoothAclDisonnectRequested
                    )
                )
            }
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                bluetoothAclConnectedStatus_.postValue(SingleLiveDataEvent(BluetoothAclDisonnected))
            }
        }
    }
}
