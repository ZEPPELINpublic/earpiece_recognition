package jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothLeConnectBase

/**
 * Contract I/F for BLE device data read (e.g. battery level etc)
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface IBleContract {
    val batteryPctLive: LiveData<Int>
    val bluetoothLeConnectStatus: LiveData<SingleLiveDataEvent<BluetoothLeConnectBase>>

    interface IBleImplContract : IBleContract {
        fun connectBleDevice(context: Context?, device: BluetoothDevice?)
    }

    interface IBleUiContract : IBleContract {
        fun connectBleDevice()
    }
}
