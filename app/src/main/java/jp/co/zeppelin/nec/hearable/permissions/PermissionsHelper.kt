package jp.co.zeppelin.nec.hearable.permissions

import android.Manifest
import android.app.Activity
import androidx.lifecycle.MutableLiveData
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog.Builder
import pub.devrel.easypermissions.EasyPermissions
import java.lang.ref.WeakReference


/**
 * Top level impl for dealing with Android 'M' permissions
 *
 * Ref: https://github.com/googlesamples/easypermissions
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class PermissionsHelper(activity: Activity) :
    IPermissionsContract,
    EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {
    private val TAG = PermissionsHelper::class.java.simpleName
    private val weakRefActivity = WeakReference(activity)

    private val _locationPermissionGranted =
        MutableLiveData<SingleLiveDataEvent<IPermissionsContract.PermissionStatus>>()
    override val locationPermissionGranted = _locationPermissionGranted
    private val _cameraPermissionStatus =
        MutableLiveData<SingleLiveDataEvent<IPermissionsContract.PermissionStatus>>()
    override val cameraPermissionStatus = _cameraPermissionStatus
    private val _microphonePermissionStatus =
        MutableLiveData<SingleLiveDataEvent<IPermissionsContract.PermissionStatus>>()
    override val microphonePermissionStatus = _microphonePermissionStatus

    private fun requestBluetoothNLocationSimple(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            activity.resources.getString(R.string.permission_rationale_bluetooth),
            RC_PERM_BLUETOOTH_N_LOCATION,
            *PERMISSIONS_BLUETOOTH_N_LOCATION
        )
    }

    private fun requestPermissionCameraSimple(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            activity.resources.getString(R.string.permission_rationale_camera),
            RC_PERM_CAMERA,
            *PERMISSIONS_CAMERA
        )
    }

    private fun requestPermissionMicrophoneSimple(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            activity.resources.getString(R.string.permission_rationale_microphone),
            RC_PERM_MICROPHONE,
            *PERMISSIONS_MICROPHONE
        )
    }

    override fun isPermissionLocationGranted(): Boolean {
        var isPermissionGranted = false
        weakRefActivity.get()?.let { activity ->
            isPermissionGranted = EasyPermissions.hasPermissions(
                activity,
                *PERMISSIONS_BLUETOOTH_N_LOCATION
            )
        }
        return isPermissionGranted
    }

    override fun isPermissionCameraGranted(): Boolean {
        var isPermissionGranted = false
        weakRefActivity.get()?.let { activity ->
            isPermissionGranted = EasyPermissions.hasPermissions(
                activity,
                *PERMISSIONS_CAMERA
            )
        }
        return isPermissionGranted
    }

    override fun isPermissionMicrophoneGranted(): Boolean {
        var isPermissionGranted = false
        weakRefActivity.get()?.let { activity ->
            isPermissionGranted = EasyPermissions.hasPermissions(
                activity,
                *PERMISSIONS_MICROPHONE
            )
        }
        return isPermissionGranted
    }

    // I/F IPermissionsContract
    // @AfterPermissionGranted annotated method needs to be void and without input parameters
    @AfterPermissionGranted(RC_PERM_BLUETOOTH_N_LOCATION)
    override fun checkAndRequestPermissionsBluetoothNLocation() {
        if (isPermissionLocationGranted()) {
            ZepLog.d(TAG, "checkPermissionsBluetooth(): OK")
            _locationPermissionGranted.postValue(SingleLiveDataEvent(IPermissionsContract.PermissionStatus.Granted))
        } else {
            ZepLog.i(TAG, "checkPermissionsBluetooth(): not yet")
            weakRefActivity.get()?.let { activity ->
                requestBluetoothNLocationSimple(activity)
            }
        }
    }

    // I/F IPermissionsContract
    // @AfterPermissionGranted annotated method needs to be void and without input parameters
    @AfterPermissionGranted(RC_PERM_CAMERA)
    override fun checkAndRequestPermissionCameraForQRCodeScanning() {
        if (isPermissionCameraGranted()) {
            ZepLog.d(TAG, "checkPermissionCameraForQRCodeScanning(): OK")
            _cameraPermissionStatus.postValue(SingleLiveDataEvent(IPermissionsContract.PermissionStatus.Granted))
        } else {
            ZepLog.i(TAG, "checkPermissionCameraForQRCodeScanning(): not yet")
            weakRefActivity.get()?.let { activity ->
                requestPermissionCameraSimple(activity)
            }
        }
    }

    // I/F IPermissionsContract
    // @AfterPermissionGranted annotated method needs to be void and without input parameters
    @AfterPermissionGranted(RC_PERM_MICROPHONE)
    override fun checkAndRequestPermissionMicrophoneForVoiceMemoRecording() {
        if (isPermissionMicrophoneGranted()) {
            ZepLog.d(TAG, "checkAndRequestPermissionMicrophoneForVoiceMemoRecording(): OK")
            _microphonePermissionStatus.postValue(SingleLiveDataEvent(IPermissionsContract.PermissionStatus.Granted))
        } else {
            ZepLog.i(TAG, "checkAndRequestPermissionMicrophoneForVoiceMemoRecording(): not yet")
            weakRefActivity.get()?.let { activity ->
                requestPermissionMicrophoneSimple(activity)
            }
        }
    }

    override fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedLocation() {
        weakRefActivity.get()?.let { activity ->
            if (EasyPermissions.somePermissionPermanentlyDenied(
                    activity,
                    PERMISSIONS_BLUETOOTH_N_LOCATION.toList()
                )
            ) {
                Builder(activity)
                    .build().show()

            } else {
                checkAndRequestPermissionsBluetoothNLocation()
            }
        }
    }

    override fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedCamera() {
        weakRefActivity.get()?.let { activity ->
            if (EasyPermissions.somePermissionPermanentlyDenied(
                    activity,
                    PERMISSIONS_CAMERA.toList()
                )
            ) {
                Builder(activity)
                    .build().show()
            } else {
                checkAndRequestPermissionCameraForQRCodeScanning()
            }
        }
    }

    override fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedMicrophone() {
        weakRefActivity.get()?.let { activity ->
            if (EasyPermissions.somePermissionPermanentlyDenied(
                    activity,
                    PERMISSIONS_MICROPHONE.toList()
                )
            ) {
                Builder(activity)
                    .build().show()
            } else {
                checkAndRequestPermissionMicrophoneForVoiceMemoRecording()
            }
        }
    }

    // I/F EasyPermissions.PermissionCallbacks
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        ZepLog.d(
            TAG,
            "onRequestPermissionsResult(): $requestCode, ${permissions.forEach {
                ZepLog.d(
                    TAG,
                    "    $it"
                )
            }}, ${grantResults.forEach { ZepLog.d(TAG, "    $it") }}"
        )
    }

    // I/F EasyPermissions.PermissionCallbacks
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        ZepLog.d(TAG, "onPermissionsGranted(): $requestCode, $perms")
    }

    // I/F EasyPermissions.PermissionCallbacks
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        ZepLog.d(TAG, "onPermissionsDenied(): $requestCode, $perms")
        weakRefActivity.get()?.let { activity ->
            // App needs to shut down if essential permissions permanently denied
            when (requestCode) {
                RC_PERM_BLUETOOTH_N_LOCATION -> {
                    ZepLog.d(TAG, "onPermissionsDenied(): fire _locationPermissionGranted")
                    if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
                        Builder(activity)
                            .setRationale(R.string.permission_rationale_bluetooth)
                            .build().show()
                        _locationPermissionGranted.postValue(
                            SingleLiveDataEvent(
                                IPermissionsContract.PermissionStatus.PermanentlyDenied
                            )
                        )
                    } else {
                        _locationPermissionGranted.postValue(
                            SingleLiveDataEvent(
                                IPermissionsContract.PermissionStatus.Denied
                            )
                        )
                    }
                }
                RC_PERM_CAMERA -> {
//                    showCameraPermissionRationale()
                    if (EasyPermissions.somePermissionPermanentlyDenied(activity, perms)) {
                        Builder(activity)
                            .setRationale(R.string.permission_rationale_camera)
                            .build().show()
                        _cameraPermissionStatus.postValue(SingleLiveDataEvent(IPermissionsContract.PermissionStatus.PermanentlyDenied))
                    } else {
                        _cameraPermissionStatus.postValue(SingleLiveDataEvent(IPermissionsContract.PermissionStatus.Denied))
                    }
                }
                else -> {
                }
            }
        }
    }

    // I/F EasyPermissions.RationaleCallbacks
    override fun onRationaleAccepted(requestCode: Int) {
        ZepLog.d(TAG, "onRationaleAccepted(): $requestCode")
    }

    // I/F EasyPermissions.RationaleCallbacks
    override fun onRationaleDenied(requestCode: Int) {
        ZepLog.d(TAG, "onRationaleDenied(): $requestCode")
    }

    companion object {
        private const val RC_PERM_BLUETOOTH_N_LOCATION = 1367
        private const val RC_PERM_CAMERA = 1368
        private const val RC_PERM_MICROPHONE = 1369

        private val PERMISSIONS_BLUETOOTH_N_LOCATION = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        private val PERMISSIONS_CAMERA = arrayOf(
            Manifest.permission.CAMERA
        )
        private val PERMISSIONS_MICROPHONE = arrayOf(
            Manifest.permission.RECORD_AUDIO
        )
    }
}
