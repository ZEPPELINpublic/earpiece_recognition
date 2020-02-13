package jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.impl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract.IBluetoothContract

/**
 * Bluetooth utilities which may be used by bluetooth broadcast receiver
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class BluetoothLowLevelHelper(context: Context) : IBluetoothContract.ILowLevelImplContract {
    private val TAG = BluetoothLowLevelHelper::class.java.simpleName

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    /**
     * Check if bluetooth device is currently connected (which in turn implies paired/"bonded")
     *
     * Android handset happily remembers paired ("bonded") devices, but paired doesn't mean
     * actively connected
     *
     * NOTE: relies on impl detail via reflection, brittle over time in case API changes
     *
     * Refs:
     *      https://stackoverflow.com/questions/4715865/how-to-programmatically-tell-if-a-bluetooth-device-is-connected
     *
     */
    private fun isHearableBluetoothClassicOrBLEConnectedOldSchool(device: BluetoothDevice): Boolean {
        ZepLog.d(TAG, "checkDeviceConnectedOldSchool()")
        var isConnected = false
        try {
            val method = device.javaClass.getMethod("isConnected")
            isConnected = method.invoke(device) as Boolean
        } catch (e: Exception) {
            ZepLog.e(
                TAG,
                "checkDeviceConnectedOldSchool(): ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            )
        }
        return isConnected
    }

    override fun isPairedAndConnectedWithDeviceWMacAddr(macAddr: String): IBluetoothContract.PairedAndConnectedResult {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        var isPaired = false
        var isConnected = false
        var device: BluetoothDevice? = null
        pairedDevices?.forEach { candidateDevice ->
            if (candidateDevice.address == macAddr) {
                // If we are here, we know candidateDevice is paired ("bonded")...
                isPaired = true
                // ...now verify also "connected"
                isConnected = isHearableBluetoothClassicOrBLEConnectedOldSchool(candidateDevice)
                device = candidateDevice
            }
        }
        return IBluetoothContract.PairedAndConnectedResult(isPaired, isConnected, device)
    }

    override fun checkTargetDevicePairedAndConnectedBlocking(macAddrOrId: String?): Boolean {
        if (macAddrOrId.isNullOrEmpty()) {
            return false
        }
        return isPairedAndConnectedWithDeviceWMacAddr(DeviceId.macAddressFromDeviceId(macAddrOrId)).isPairedAndConnected()
    }
}
