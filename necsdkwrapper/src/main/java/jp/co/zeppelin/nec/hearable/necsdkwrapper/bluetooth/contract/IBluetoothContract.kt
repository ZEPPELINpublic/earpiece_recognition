package jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.model.BluetoothConnectStatusBase
import jp.co.zeppelin.nec.hearable.domain.model.BluetoothMsgBase
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothAclConnectBase

/**
 * Top level contract interface for bluetooth utilities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface IBluetoothContract {

    interface ILowLevelContract {
        fun checkTargetDevicePairedAndConnectedBlocking(macAddrOrId: String?): Boolean
    }

    data class PairedAndConnectedResult(
        val isPaired: Boolean,
        val isConnected: Boolean,
        val device: BluetoothDevice?
    ) {
        fun isPairedAndConnected(): Boolean = isPaired && isConnected
    }

    interface ILowLevelImplContract : ILowLevelContract {
        fun isPairedAndConnectedWithDeviceWMacAddr(macAddr: String): PairedAndConnectedResult
    }

    interface IBcReceiverContract : IBluetoothContract {
        val bluetoothAclConnectedStatus: LiveData<SingleLiveDataEvent<BluetoothAclConnectBase>>
    }

    interface IHelperContract : IBluetoothContract {
        val isBluetoothEnabled: LiveData<SingleLiveDataEvent<Boolean>>
        val targetDeviceBluetoothConnectionStatus: LiveData<SingleLiveDataEvent<BluetoothConnectStatusBase>>
        val targetLEDeviceFound: LiveData<SingleLiveDataEvent<BluetoothMsgBase>>

        fun updateTargetDeviceId(deviceId: String)

        /**
         * Note: no timeout required, classic bluetooth discovery will actually send signal when complete
         */
        fun launchBluetoothClassicDiscovery()

        /**
         * Start bluetooth LE device discovery with timeout; stop automatically when timeout elapsed
         *
         * @return progress bar timer observable
         */

        /**
         * Trigger/observer pair: updateBluetoothEnabledStatus() triggers result delivered via observable isBluetoothEnabled
         */
        fun updateBluetoothEnabledStatus()
    }


    interface IImplContract : IHelperContract, ILowLevelContract, IBcReceiverContract {
        suspend fun launchBluetoothLEDiscovery()

        /**
         * Sanity check that NEC Hearable device is in fact paired and connected to
         * android handset
         *
         * (Empirically it is possible to connect via BLE to hearable device which is powered on
         * but neither paired nor connected to android hanset)
         */
        suspend fun checkTargetDevicePairedAndConnected(macAddrOrId: String)
    }

    interface IUIContract : IHelperContract, IBcReceiverContract {
        fun hearableDeviceId(): String?

        fun startBluetoothLeDiscovery()

        fun checkTargetDevicePairedAndConnected()
    }
}
