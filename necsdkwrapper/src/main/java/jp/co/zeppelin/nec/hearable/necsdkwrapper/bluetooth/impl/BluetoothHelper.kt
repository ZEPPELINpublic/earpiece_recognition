package jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import androidx.lifecycle.MutableLiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.helpers.ThreadHelper
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.*
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract.IBluetoothContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.constants.NecHearableSDKConstants

/**
 * Top level impl for bluetooth utilities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.ExperimentalCoroutinesApi
@kotlinx.coroutines.InternalCoroutinesApi
class BluetoothHelper(context: Context) : IBluetoothContract.IImplContract {
    private val TAG = BluetoothHelper::class.java.simpleName

    private val receiver = BluetoothBroadcastReceiver(context)

    override val bluetoothAclConnectedStatus = receiver.bluetoothAclConnectedStatus

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bluetoothLowLevelHelper: IBluetoothContract.ILowLevelImplContract =
        BluetoothLowLevelHelper(context)

    private val isBluetoothEnabled_ = MutableLiveData<SingleLiveDataEvent<Boolean>>()
    override val isBluetoothEnabled = isBluetoothEnabled_

    private val targetDeviceBluetoothConnectionStatus_ =
        MutableLiveData<SingleLiveDataEvent<BluetoothConnectStatusBase>>()
    override val targetDeviceBluetoothConnectionStatus = targetDeviceBluetoothConnectionStatus_

    private val targetLEDeviceFound_ = MutableLiveData<SingleLiveDataEvent<BluetoothMsgBase>>()
    override val targetLEDeviceFound = targetLEDeviceFound_

    private var targetDeviceId: String = ""
    private var targetDeviceFoundViaScan: BluetoothDevice? = null


    override fun checkTargetDevicePairedAndConnectedBlocking(macAddrOrId: String?): Boolean =
        bluetoothLowLevelHelper.checkTargetDevicePairedAndConnectedBlocking(macAddrOrId)

    override suspend fun checkTargetDevicePairedAndConnected(macAddrOrId: String) {
        val macAddr = DeviceId.macAddressFromDeviceId(macAddrOrId)
        if (macAddr.isNotEmpty()) {
            // Sanity check; have observed that "is connected" will come back true
            // when NEC hearable device is in "pairing" mode, but not yet paired
            val pairedAndConnectedResult =
                bluetoothLowLevelHelper.isPairedAndConnectedWithDeviceWMacAddr(macAddr)
            if (pairedAndConnectedResult.isPairedAndConnected()) {
                targetDeviceBluetoothConnectionStatus_.postValue(
                    SingleLiveDataEvent(
                        PairedAndConnected(
                            targetMacAddr = macAddr,
                            device = pairedAndConnectedResult.device
                        )
                    )
                )
            } else {
                ZepLog.i(
                    TAG,
                    "checkTargetDevicePairedAndConnected(): WARNING: paired-and-connected device not found!: $pairedAndConnectedResult"
                )
                targetDeviceBluetoothConnectionStatus_.postValue(
                    SingleLiveDataEvent(
                        NotPairedAndConnected(
                            targetMacAddr = macAddr,
                            device = pairedAndConnectedResult.device
                        )
                    )
                )
            }
        } else {
            throw AssertionError("$TAG::checkTargetDevicePairedAndConnected(): ERROR: empty MAC address!")
        }
    }

    override fun launchBluetoothClassicDiscovery() {
        ZepLog.d(TAG, "launchBluetoothClassicDiscovery(): intro")
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        btAdapter.startDiscovery()
    }


    /**
     * NOTE: have observed that sometimes device name is null even for the NH1
     *
     * Note: _MUST_ use same callback object for start and stop scan, otherwise it will go all "Sorcerer's Apprentice" on your a**
     * typ:
     * callbackType 1
     * ScanResult{
     *      device=00:0A:45:10:DF:DF,
     *      scanRecord=ScanRecord [mAdvertiseFlags=2, mServiceUuids=[00001200-f207-4b13-abd9-6cc3e42fd56b],
     *          mManufacturerSpecificData={34=[1]},
     *          mServiceData={}, mTxPowerLevel=-2147483648, mDeviceName=NH1],
     *      rssi=-50, timestampNanos=91172993315983, eventType=27, primaryPhy=1, secondaryPhy=0, advertisingSid=255, txPower=127, periodicAdvertisingInterval=0}
     * callbackType 1
     * ScanResult{
     *      device=28:E1:7C:26:D5:C4,
     *      scanRecord=ScanRecord [mAdvertiseFlags=-1, mServiceUuids=null,
     *          mManufacturerSpecificData={6=[1, 9, 32, 2, -7, 72, 108, -101, -32, -13, -104, -20, 97, 95, 119, -8, -124, 32, 125, 41, 80, -93, -82, -94, -122, 122, 44]},
     *          mServiceData={}, mTxPowerLevel=-2147483648, mDeviceName=null],
     *      rssi=-71, timestampNanos=91173004998587, eventType=16, primaryPhy=1, secondaryPhy=0, advertisingSid=255, txPower=127, periodicAdvertisingInterval=0}
     */
    private val bluetoothLeScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            ZepLog.d(
                TAG,
                "bluetoothLeScanCallback::onScanResult(): callbackType $callbackType, result $result"
            )
            result?.apply {
                if (device.address == DeviceId.macAddressFromDeviceId(targetDeviceId)) {
                    targetDeviceFoundViaScan = device
                    targetLEDeviceFound_.postValue(
                        SingleLiveDataEvent(
                            BluetoothTargetDeviceFound(
                                device
                            )
                        )
                    )
                    ZepLog.d(
                        TAG,
                        "bluetoothLeScanCallback::onScanResult(): **** Found target hearable device, stopping scan! **** ${ThreadHelper.threadId(
                            Thread.currentThread()
                        )}"
                    )
                    stopBluetoothLEDiscovery("target device found", isOK = true)
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            ZepLog.d(
                TAG,
                "bluetoothLeScanCallback::onBatchScanResults(): ${results?.size} results ${ThreadHelper.threadId(
                    Thread.currentThread()
                )}"
            )
            results?.forEach { result -> ZepLog.d(TAG, "    $result") }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            ZepLog.e(
                TAG,
                "bluetoothLeScanCallback::onScanFailed(): ${NecHearableSDKConstants.bluetoothLeErrorCodesHumanReadableMap[errorCode]} ${ThreadHelper.threadId(
                    Thread.currentThread()
                )}"
            )
        }
    }

    private fun stopBluetoothLEDiscovery(tag: String, isOK: Boolean) {
        if (!isOK) {
            ZepLog.e(TAG, "stopBluetoothLEDiscovery(): WARNING: $tag")
        }
        bluetoothAdapter.apply {
            bluetoothLeScanner.stopScan(bluetoothLeScanCallback)
        }
    }

    override suspend fun launchBluetoothLEDiscovery() {
        bluetoothAdapter.apply {
            val scanFilters = mutableListOf<ScanFilter>()
            try {
                scanFilters.add(
                    ScanFilter.Builder()
                        .setDeviceAddress(DeviceId.macAddressFromDeviceId(targetDeviceId))
                        .build()
                )
                val scanSettings = ScanSettings.Builder().build()
                // Gracefully handle handset bluetooth switched-off case
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.startScan(scanFilters, scanSettings, bluetoothLeScanCallback)
                } else {
                    ZepLog.e(TAG, "launchBluetoothLEDiscovery(): ERROR: system BT LE scanner null!")
                }
            } catch (e: Exception) {
                ZepLog.e(
                    TAG,
                    "launchBluetoothLEDiscovery(): ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
                )
            }
        }
    }

    override fun updateBluetoothEnabledStatus() {
        isBluetoothEnabled_.postValue(SingleLiveDataEvent(bluetoothAdapter.isEnabled))
    }

    override fun updateTargetDeviceId(deviceId: String) {
        targetDeviceId = deviceId
    }
}
