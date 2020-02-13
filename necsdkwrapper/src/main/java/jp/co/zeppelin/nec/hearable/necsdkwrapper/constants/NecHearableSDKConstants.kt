package jp.co.zeppelin.nec.hearable.necsdkwrapper.constants

import android.bluetooth.le.ScanCallback
import com.nec.Android.hearable.HearableErrorCode

/**
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
object NecHearableSDKConstants {

    // Ref: https://developer.android.com/guide/topics/connectivity/bluetooth-le
    const val REQUEST_ENABLE_BT = 3789

    const val bluetoothLeConnectTimeoutSecs = 10L
    const val requestAuthTimeoutSecs = 15L

    val bluetoothLeErrorCodesHumanReadableMap = mapOf(
        ScanCallback.SCAN_FAILED_ALREADY_STARTED to "SCAN_FAILED_ALREADY_STARTED",
        ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED to "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED",
        ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED to "SCAN_FAILED_FEATURE_UNSUPPORTED",
        ScanCallback.SCAN_FAILED_INTERNAL_ERROR to "SCAN_FAILED_INTERNAL_ERROR"
    )

    /**
     * Empirically MQTT init requires ~5 sec
     */
    const val MQTT_INIT_TIMEOUT_SECS = 10L

    const val NEC_HEARABLE_STATUS_OK = 0

    enum class NecHearableSDKErrorCodes {
        OK
    }

    // NEC SDK error codes for human readability
    val necHearableSDKErrorCodesHumanReadableMap = mapOf(
        NecHearableSDKErrorCodes.OK.ordinal to "OK",
        HearableErrorCode.BLE_ALREADY_CONNECTED to "BLE_ALREADY_CONNECTED",
        HearableErrorCode.BLE_CHARACTERISTIC_READ_ERROR to "BLE_CHARACTERISTIC_READ_ERROR",
        HearableErrorCode.BLE_CHARACTERISTIC_WRITE_ERROR to "BLE_CHARACTERISTIC_WRITE_ERROR",
        HearableErrorCode.BLE_CONNECTION_OPERATION_ERROR to "BLE_CONNECTION_OPERATION_ERROR",
        HearableErrorCode.BLE_DESCRIPTOR_WRITE_ERROR to "BLE_DESCRIPTOR_WRITE_ERROR",
        HearableErrorCode.BLE_DISCONNECTED to "BLE_DISCONNECTED",
        HearableErrorCode.BLE_NOT_FOUND_SERVICE to "BLE_NOT_FOUND_SERVICE",
        HearableErrorCode.BLE_SERVICE_DISCOVER_ERROR to "BLE_SERVICE_DISCOVER_ERROR",
        HearableErrorCode.BLUETOOTH_DISABLED to "BLUETOOTH_DISABLED",
        HearableErrorCode.BLUETOOTH_IS_BUSY to "BLUETOOTH_IS_BUSY",
        HearableErrorCode.FAILED_CONNECT_SCO_DEVICE to "FAILED_CONNECT_SCO_DEVICE",
        HearableErrorCode.FAILED_FIND_CONNECT_BLE_DEVICE to "FAILED_FIND_CONNECT_BLE_DEVICE",
        HearableErrorCode.FAILED_MQTT_REQUEST to "FAILED_MQTT_REQUEST",
        HearableErrorCode.FAILED_NOT_INITILIZE_MQTT to "FAILED_NOT_INITILIZE_MQTT",
        HearableErrorCode.FAILED_NOT_SET_USERID to "FAILED_NOT_SET_USERID",
        HearableErrorCode.FAILED_STATUS_UNMATCH to "FAILED_STATUS_UNMATCH",
        HearableErrorCode.REQUEST_TIMEOUT to "REQUEST_TIMEOUT",
        HearableErrorCode.STATUS_FAILED_BLE_KEY_EXCHANGE to "STATUS_FAILED_BLE_KEY_EXCHANGE",
        HearableErrorCode.STATUS_FAILED_BLE_PAIRED to "STATUS_FAILED_BLE_PAIRED",
        HearableErrorCode.UNKNOWN to "UNKNOWN",
        HearableErrorCode.UNREACHED_REQUEST to "UNREACHED_REQUEST",
        HearableErrorCode.UNSUPPORTED_BLUETOOTH to "UNSUPPORTED_BLUETOOTH"
    )
}
