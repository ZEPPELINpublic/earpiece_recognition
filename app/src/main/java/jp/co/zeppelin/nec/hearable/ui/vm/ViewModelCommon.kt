package jp.co.zeppelin.nec.hearable.ui.vm

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import jp.co.zeppelin.nec.hearable.data.contract.IDataRepo
import jp.co.zeppelin.nec.hearable.data.impl.DataRepoImpl
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.data.HearableWearerUsernameIds
import jp.co.zeppelin.nec.hearable.navigation.INavigationContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract.IBleContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.contract.IBluetoothContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.impl.BluetoothHelper
import jp.co.zeppelin.nec.hearable.necsdkwrapper.bluetooth.impl.BluetoothLeHelper
import jp.co.zeppelin.nec.hearable.necsdkwrapper.constants.NecHearableSDKConstants
import jp.co.zeppelin.nec.hearable.necsdkwrapper.contract.INecSdkContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.coro.ColdFlowWTimeout
import jp.co.zeppelin.nec.hearable.necsdkwrapper.impl.NecHearableSdkWrapper
import jp.co.zeppelin.nec.hearable.permissions.IPermissionsContract
import jp.co.zeppelin.nec.hearable.permissions.PermissionsHelper
import jp.co.zeppelin.nec.hearable.prefs.HearableSharedPrefs
import jp.co.zeppelin.nec.hearable.ui.settings.ISettingsContract
import jp.co.zeppelin.nec.hearable.voicememo.ColdFlowCountdown
import jp.co.zeppelin.nec.hearable.voicememo.IVoiceMemoContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Top level shared AAC viewModel across activities/fragments
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ViewModelCommon(activity: FragmentActivity) : ViewModel(),
    IDataRepo.IDataRepoUI,
    IPermissionsContract,
    IBluetoothContract.IUIContract,
    ISettingsContract,
    INavigationContract,
    INecSdkContract.INecHearableSdkUIContract,
    IBleContract.IBleUiContract,
    IVoiceMemoContract {
    private val TAG = ViewModelCommon::class.java.simpleName

    private val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(activity.applicationContext)

    private val hearableSharedPrefs: ISettingsContract =
        HearableSharedPrefs(activity.applicationContext)

    private val bluetoothHelper = BluetoothHelper(activity.applicationContext)
    private val weakRefContext = WeakReference(activity.applicationContext)
    private val bluetoothLeHelper = BluetoothLeHelper()

    override val isBluetoothEnabled = bluetoothHelper.isBluetoothEnabled
    override val targetDeviceBluetoothConnectionStatus =
        bluetoothHelper.targetDeviceBluetoothConnectionStatus
    override val targetLEDeviceFound = bluetoothHelper.targetLEDeviceFound
    val permissionsHelper: IPermissionsContract = PermissionsHelper(activity)
    override val locationPermissionGranted = permissionsHelper.locationPermissionGranted
    override val cameraPermissionStatus = permissionsHelper.cameraPermissionStatus
    override val microphonePermissionStatus = permissionsHelper.microphonePermissionStatus

    private val necHearableStarted_ = MutableLiveData<Boolean>()
    val necHearableStarted: LiveData<Boolean> = necHearableStarted_

    var targetDevice: BluetoothDevice? = null

    override lateinit var loginSignup: INavigationContract.LoginSignup

    private val necHearableSdkWrapper: INecSdkContract.INecHearableSdkImplContract =
        NecHearableSdkWrapper(activity.applicationContext)
    override val hearableStatus = necHearableSdkWrapper.hearableStatus
    override val mqttInitStatus = necHearableSdkWrapper.mqttInitStatus
    override val bluetoothAclConnectedStatus = bluetoothHelper.bluetoothAclConnectedStatus
    override val bluetoothLeConnected = necHearableSdkWrapper.bluetoothLeConnected
    override val bluetoothLeDisconnected = necHearableSdkWrapper.bluetoothLeDisconnected
    override val necWearerIdResponse = necHearableSdkWrapper.necWearerIdResponse
    override val necHearableSdkError = necHearableSdkWrapper.necHearableSdkError
    override val necHearableEarFeatureMeasureResponse =
        necHearableSdkWrapper.necHearableEarFeatureMeasureResponse
    override val necHearableEarFeatureServerSendResponse =
        necHearableSdkWrapper.necHearableEarFeatureServerSendResponse
    override val sensorDataPrepResponse = necHearableSdkWrapper.sensorDataPrepResponse
    override val sensorDataSendResponse = necHearableSdkWrapper.sensorDataSendResponse
    override val sensorDataStopSendResponse = necHearableSdkWrapper.sensorDataStopSendResponse
    override val necHearableAuthSubscribeResponse =
        necHearableSdkWrapper.necHearableAuthSubscribeResponse
    override val necHearableVoiceMemoRecordResponse =
        necHearableSdkWrapper.necHearableVoiceMemoRecordResponse
    override val necHearableVoiceMemoUploadResponse =
        necHearableSdkWrapper.necHearableVoiceMemoUploadResponse


    init {
        necHearableStarted_.postValue(true)
    }

    override val edgeId = necHearableSdkWrapper.edgeId
    override var wearerId = ""
    override fun setSdkWearerId() {
        necHearableSdkWrapper.setWearerId(wearerId)
    }

    var niceUsername = ""

    override val hearableDatasetInsertedInDB = liveData(Dispatchers.IO) {
        val hearableAndWearerId =
            dataRepo.insertHearableWearerIdPair(lastTargetDeviceId(), wearerId)
        val hearableAndNiceUsernameId =
            dataRepo.insertHearableUsernamePair(lastTargetDeviceId(), niceUsername)
        val retval = HearableWearerUsernameIds(
            hearableAndWearerId.hearableId,
            hearableAndWearerId.wearerId,
            hearableAndNiceUsernameId.niceUsernameId
        )
        ZepLog.d(TAG, "hearableDatasetInserted: insert complete: $retval")
        emit(retval)
    }

    override val registeredHearableDatasetsFromDB = liveData(Dispatchers.IO) {
        dataRepo.getAllHearableDatasets().collect { entities ->
            emit(entities.filter { it.hearableId.isAuthenticated })
        }
    }

    override val hearableDatasetFromDBForHearableID = liveData(Dispatchers.IO) {
        ZepLog.d(TAG, "hearableDatasetFromDBForHearableID: intro")
        dataRepo.getHearableDatasetFor(lastTargetDeviceId()).collect { hearableAssocOneToOne ->
            ZepLog.d(TAG, "hearableDatasetFromDBForHearableID: emitting $hearableAssocOneToOne")
            emit(SingleLiveDataEvent(hearableAssocOneToOne))
        }
    }

    override val hearableRegistered = liveData(Dispatchers.IO) {
        dataRepo.updateAuthenticateHearableIdFor(lastTargetDeviceId()).collect { numRowsUpdated ->
            emit(numRowsUpdated)
        }
    }

    override fun checkNiceUsernameAlreadyInUse(niceUsername: String, action: (Boolean) -> Unit) {
        viewModelScope.launch {
            dataRepo.getAllNiceUsernames().collect { niceUserNames ->
                val foundNiceUsername = niceUserNames.find { it.niceUsername == niceUsername }
                action(foundNiceUsername != null)
            }
        }
    }

    override fun voiceMemoRecordTimer(recordTimeSecs: Long): ColdFlowCountdown {
        return ColdFlowCountdown(recordTimeSecs,
            "voiceMemoRecordTimer",
            { }) {}
    }

    private fun startVoiceMemoRecord(autoStopSecs: Double) {
        weakRefContext.get()?.let { context ->
            necHearableSdkWrapper.voiceMemoPrepareRecord(
                context, wearerId, autoStopSecs, viewModelScope
            )
        }
    }

    override fun startRecording(autoStopSecs: Double) {
        if (wearerId.isEmpty()) {
            ZepLog.d(
                TAG,
                "startRecording(): WARNING: empty wearer ID, possibly re-entered app after changing permissions in system settings!  Attempting to get from DB..."
            )
            viewModelScope.launch {
                dataRepo.getHearableDatasetFor(lastTargetDeviceId()).collect { respObj ->
                    if (respObj != null) {
                        wearerId = respObj.wearerId
                        startVoiceMemoRecord(autoStopSecs)
                    } else {
                        throw AssertionError("startRecording(): failed to get wearer ID from DB!")
                    }
                }
            }
        } else {
            startVoiceMemoRecord(autoStopSecs)
        }
    }

    override fun stopRecording() {
        necHearableSdkWrapper.voiceMemoStopRecording()
    }

    val cancelRecording_ = MutableLiveData<SingleLiveDataEvent<Boolean>>()
    override val cancelRecording = cancelRecording_

    override fun cancelRecording() {
        ZepLog.d(TAG, "cancelRecording()")
        cancelRecording_.postValue(SingleLiveDataEvent(true))
        necHearableSdkWrapper.voiceMemoCancelRecording()
    }

    override val batteryPctLive = liveData(Dispatchers.IO) {
        ZepLog.d(TAG, "re-emit batteryPctLive: ${bluetoothLeHelper.batteryPctLive}")
        emitSource(bluetoothLeHelper.batteryPctLive)
    }

    override val bluetoothLeConnectStatus = liveData(Dispatchers.IO) {
        emitSource(bluetoothLeHelper.bluetoothLeConnectStatus)
    }

    // I/F IBleContract.IBleUiContract
    override fun connectBleDevice() {
        bluetoothLeHelper.connectBleDevice(weakRefContext.get(), targetDevice)
    }

    override fun hearableDeviceId(): String? = targetDevice?.address

    override fun startService(context: Context) = necHearableSdkWrapper.startService(context)
    override fun setMqttParameter() = necHearableSdkWrapper.setMqttParameter()

    override fun checkTargetDevicePairedAndConnected() {
        viewModelScope.launch {
            bluetoothHelper.checkTargetDevicePairedAndConnected(
                lastTargetDeviceId()
            )
        }
    }

    override fun attemptConnectBluetoothLeFlow(): ColdFlowWTimeout {
        return ColdFlowWTimeout(
            NecHearableSDKConstants.bluetoothLeConnectTimeoutSecs,
            "attemptConnectBluetoothLe",
            {
                necHearableSdkWrapper.connectBleToExplicitMacAddr(
                    DeviceId.macAddressFromDeviceId(
                        lastTargetDeviceId()
                    )
                )
            }) {}
    }

    override fun attemptInitMqttFlow(): ColdFlowWTimeout {
        return ColdFlowWTimeout(NecHearableSDKConstants.MQTT_INIT_TIMEOUT_SECS,
            "attemptInitMQTT",
            { setMqttParameter() }) {}
    }

    override fun requestWearerId() = necHearableSdkWrapper.requestWearerId()

    override fun attemptMeasureEarFeature() = necHearableSdkWrapper.attemptMeasureEarFeature()
    override fun attemptSendEarFeatureToServer() =
        necHearableSdkWrapper.attemptSendEarFeatureToServer()

    override fun registerEarTimeoutFlow() = necHearableSdkWrapper.registerEarTimeoutFlow()

    override fun requestAuthFlow(wearerId: String) = necHearableSdkWrapper.requestAuthFlow(wearerId)

    override fun startSendSensorData(doSendNineAxisSensor: Boolean, doSendTempSensor: Boolean) =
        necHearableSdkWrapper.startSendSensorData(doSendNineAxisSensor, doSendTempSensor)

    override fun stopSendSensorData() = necHearableSdkWrapper.stopSendSensorData()

    override fun cancelSubscribe() = necHearableSdkWrapper.cancelSubscribe()

    override fun registHearableStatusListener() =
        necHearableSdkWrapper.registHearableStatusListener()

    override fun unregisterHearableStatusListener() =
        necHearableSdkWrapper.unregisterHearableStatusListener()

    override fun requestNotifyCurrentStatus() = necHearableSdkWrapper.requestNotifyCurrentStatus()

    override fun disconnectBle() = necHearableSdkWrapper.disconnectBle()
    override fun stopService() = necHearableSdkWrapper.stopService()

    override fun launchBluetoothClassicDiscovery() {
        bluetoothHelper.launchBluetoothClassicDiscovery()
    }

    override fun startBluetoothLeDiscovery() {
        viewModelScope.launch {
            bluetoothHelper.launchBluetoothLEDiscovery()
        }
    }

    fun doActionAfterDelay(delayMilliSec: Long, action: () -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            delay(delayMilliSec)
            action()
        }
    }

    override fun updateBluetoothEnabledStatus() {
        bluetoothHelper.updateBluetoothEnabledStatus()
    }

    override fun updateTargetDeviceId(deviceId: String) {
        setLastTargetDeviceId(deviceId)
        bluetoothHelper.updateTargetDeviceId(deviceId)
    }

    // I/F ISettingsContract
    override fun lastTargetDeviceId(): String = hearableSharedPrefs.lastTargetDeviceId()

    override fun setLastTargetDeviceId(deviceId: String) =
        hearableSharedPrefs.setLastTargetDeviceId(deviceId)

    override fun isPermissionLocationGranted(): Boolean =
        permissionsHelper.isPermissionLocationGranted()

    override fun isPermissionCameraGranted(): Boolean =
        permissionsHelper.isPermissionCameraGranted()

    override fun isPermissionMicrophoneGranted() = permissionsHelper.isPermissionMicrophoneGranted()

    override fun checkAndRequestPermissionsBluetoothNLocation() =
        permissionsHelper.checkAndRequestPermissionsBluetoothNLocation()

    override fun checkAndRequestPermissionCameraForQRCodeScanning() =
        permissionsHelper.checkAndRequestPermissionCameraForQRCodeScanning()

    override fun checkAndRequestPermissionMicrophoneForVoiceMemoRecording() =
        permissionsHelper.checkAndRequestPermissionMicrophoneForVoiceMemoRecording()

    override fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedLocation() =
        permissionsHelper.checkPermissionOrTakeToSettingsIfPermanentlyDeniedLocation()

    override fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedCamera() =
        permissionsHelper.checkPermissionOrTakeToSettingsIfPermanentlyDeniedCamera()

    override fun checkPermissionOrTakeToSettingsIfPermanentlyDeniedMicrophone() =
        permissionsHelper.checkPermissionOrTakeToSettingsIfPermanentlyDeniedMicrophone()

    override fun onCleared() {
        stopService()
        super.onCleared()
    }
}

@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ViewModelCommonFactory(private val activity: FragmentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModelCommon::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewModelCommon(activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
