package jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.impl

import android.bluetooth.*
import android.content.Context
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract.IBleContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothLeConnectBase
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothLeConnected
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.BluetoothLeDisconnected
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * Retrieve information from BLE device (e.g. battery charge level etc)
 *
 * Refs:
 *      https://www.bluetooth.com/specifications/gatt/characteristics/
 *      https://medium.com/@avigezerit/bluetooth-low-energy-on-android-22bc7310387a
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class BluetoothLeHelper : IBleContract.IBleImplContract {
    var bluetoothGatt: BluetoothGatt? = null
    private val bleCharacteristics: MutableList<BluetoothGattCharacteristic> = mutableListOf()

    private val bluetoothLeConnectStatus_ =
        MutableLiveData<SingleLiveDataEvent<BluetoothLeConnectBase>>()
    override val bluetoothLeConnectStatus = bluetoothLeConnectStatus_
    private val batteryPctLive_ = MutableLiveData<Int>()
    override val batteryPctLive = liveData(Dispatchers.IO) {
        ZepLog.d(TAG, "emit batteryPctLive: ${batteryPctLive_.value}")
        emitSource(batteryPctLive_)
    }
    private var batteryPct = -1
    // I/F IBleContract.IBleImplContract
    override fun connectBleDevice(context: Context?, device: BluetoothDevice?) {
        val doAutoConnect = true
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Ref: https://github.com/android/connectivity-samples/issues/18 (fix status "133" connect failure)
            device?.connectGatt(
                context,
                doAutoConnect,
                bluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            device?.connectGatt(context, doAutoConnect, bluetoothGattCallback)
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    bluetoothLeConnectStatus_.postValue(SingleLiveDataEvent(BluetoothLeConnected))
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    bluetoothLeConnectStatus_.postValue(SingleLiveDataEvent(BluetoothLeDisconnected))
                    ZepLog.i(
                        TAG,
                        "bluetoothGattCallback::onConnectionStateChange(): NOTICE: ${btProfileStatusHumanReadableMap[newState]}!"
                    )
                }
                else -> super.onConnectionStateChange(gatt, status, newState)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    displayGattServices(gatt?.services)
                }
                else -> super.onServicesDiscovered(gatt, status)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            when (characteristic?.uuid) {
                uuidFromIntHACK(batteryLevelId) -> {
                    // https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.battery_level.xml
                    batteryPct =
                        characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                            ?: -1
                    batteryPctLive_.postValue(batteryPct)
                }
                else -> {
                }
            }
            readNextCharacteristic()
        }
    }

    /**
     * Pop next BLE characteristic from list and request read
     *
     * Note: only allowed to read one at a time
     */
    private fun readNextCharacteristic() {
//        ZepLog.d(TAG, "readNextCharacteristic(): ${bleCharacteristics.size} items remaining")
        var readCharResult = false
        while (!readCharResult) {
            if (bleCharacteristics.size < 1) {
                readCharResult = true
            } else {
                val char = bleCharacteristics.removeAt(0)
                readCharResult = bluetoothGatt?.readCharacteristic(char) ?: true
            }
        }
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
            mutableListOf()
        val gattCharacteristics = mutableListOf<BluetoothGattCharacteristic>()

        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            gattServiceData += currentServiceData

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristicsInner = gattService.characteristics

            gattCharacteristicsInner.forEach { gattCharacteristic ->
                bleCharacteristics += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                gattCharacteristicGroupData += currentCharaData
            }
            gattCharacteristics += bleCharacteristics
            gattCharacteristicData += gattCharacteristicGroupData
        }
        readNextCharacteristic()
    }

    companion object {
        private val TAG = BluetoothLeHelper::class.java.simpleName

        const val batteryLevelId = 0x2A19

        /**
         * Get BLE "Characteristic" UUID from short int from BLE spec
         *
         * Refs:
         *      https://medium.com/@avigezerit/bluetooth-low-energy-on-android-22bc7310387a
         *      https://www.bluetooth.com/specifications/gatt/characteristics/
         *
         */
        private fun uuidFromIntHACK(bleFeatureId: Int): UUID? {
            val msb = 0x0000000000001000L
            val lsb = -0x7fffff7fa064cb05L
            val value = (bleFeatureId and ((-0x1).toLong()).toInt()).toLong()
            val uuid = UUID(msb or (value shl 32), lsb)
            // Typ (for battery level) uuid 00002a19-0000-1000-8000-00805f9b34fb, value: 10777, msb 4096, lsb -9223371485494954757
//            ZepLog.d(TAG, "uuidFromIntHACK(): uuid: $uuid, value: ${value.hexAndDec()}, msb ${msb.hexAndDec()}, lsb ${lsb.hexAndDec()}")
            return uuid
        }

        val btProfileStatusHumanReadableMap = mapOf(
            BluetoothProfile.STATE_CONNECTED to "STATE_CONNECTED",
            BluetoothProfile.EXTRA_PREVIOUS_STATE to "EXTRA_PREVIOUS_STATE",
            BluetoothProfile.EXTRA_STATE to "EXTRA_STATE",
            BluetoothProfile.GATT to "GATT",
            BluetoothProfile.GATT_SERVER to "GATT_SERVER",
            BluetoothProfile.HEADSET to "HEADSET",
            // API 29
            BluetoothProfile.HEARING_AID to "HEARING_AID",
            // API 28
            BluetoothProfile.HID_DEVICE to "HID_DEVICE",
            // API 23
            BluetoothProfile.SAP to "SAP",
            BluetoothProfile.STATE_CONNECTING to "STATE_CONNECTING",
            BluetoothProfile.STATE_DISCONNECTED to "STATE_DISCONNECTED",
            BluetoothProfile.STATE_DISCONNECTING to "STATE_DISCONNECTING"
        )
    }
}
