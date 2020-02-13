package jp.co.zeppelin.nec.hearable.permissions

import androidx.lifecycle.LiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent

/**
 * Top level contract I/F for dealing with Android 'M' permissions
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface IPermissionsContract {
    enum class PermissionStatus {
        Granted,
        Denied,
        PermanentlyDenied
    }

    val locationPermissionGranted: LiveData<SingleLiveDataEvent<PermissionStatus>>
    val cameraPermissionStatus: LiveData<SingleLiveDataEvent<PermissionStatus>>
    val microphonePermissionStatus: LiveData<SingleLiveDataEvent<PermissionStatus>>

    /**
     * Counterintuitively, location permission required for Bluetooth
     */
    fun isPermissionLocationGranted(): Boolean

    /**
     * Camera permission required for scanning QR codes
     */
    fun isPermissionCameraGranted(): Boolean

    /**
     * Microphone permission required for recording VoiceMemos via NEC hearable
     */
    fun isPermissionMicrophoneGranted(): Boolean

    // Essential permissions: app has no purpose w/o ability to communicate w/external earpiece hardware
    fun checkAndRequestPermissionsBluetoothNLocation()

    // Optional permission: camera necessary for QR code scanning but alternate keyboard flow available
    fun checkAndRequestPermissionCameraForQRCodeScanning()

    // Optional permission: microphone necessary for recording voiceMemos via NEC hearable
    fun checkAndRequestPermissionMicrophoneForVoiceMemoRecording()

    // Request location permission if not permanently denied; if permanently denied go to system settings
    fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedLocation()

    // Request camera permission if not permanently denied; if permanently denied go to system settings
    fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedCamera()

    // Request microphone permission if not permanently denied; if permanently denied go to system settings
    fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedMicrophone()
}
