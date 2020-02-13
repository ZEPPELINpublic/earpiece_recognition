package jp.co.zeppelin.nec.hearable.necsdkwrapper.impl

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import com.nec.Android.hearable.*
import com.nec.Android.hearable.log.LogUtils
import com.nec.Android.hearable.mqtt.MqttServerProperty
import com.nec.Android.hearable.sensor.HearableSensorManager
import com.nec.Android.hearable.voice.HearableRegistVoice
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.NecHearableEventBase
import jp.co.zeppelin.nec.hearable.domain.model.NecHearableSdkError
import jp.co.zeppelin.nec.hearable.necsdkwrapper.R
import jp.co.zeppelin.nec.hearable.necsdkwrapper.constants.NecHearableSDKConstants
import jp.co.zeppelin.nec.hearable.necsdkwrapper.contract.INecSdkContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.coro.ColdFlowWTimeout
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference

/**
 * Abstraction layer for NEC Hearable SDK
 *
 * Request/emit paradigm: call method and observe subsequent emit of typ. LiveData
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class NecHearableSdkWrapper(context: Context) : INecSdkContract.INecHearableSdkImplContract {

    private val _hearableStatus = MutableLiveData<IHearableStatus>()
    override val hearableStatus = _hearableStatus
    private val _mqttInitStatus = MutableLiveData<NecHearableMqttBase>()
    override val mqttInitStatus = _mqttInitStatus
    private val _bluetoothLeConnected = MutableLiveData<NecHearableSimpleBase>()
    override val bluetoothLeConnected = _bluetoothLeConnected
    private val _bluetoothLeDisconnected = MutableLiveData<NecHearableSimpleBase>()
    override val bluetoothLeDisconnected = _bluetoothLeDisconnected
    private val _necWearerIdResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableWearerIdRespBase>>()
    override val necWearerIdResponse = _necWearerIdResponse
    private val _necHearableSdkError = MutableLiveData<NecHearableEventBase>()
    override val necHearableSdkError = _necHearableSdkError
    private val _necHearableEarFeatureMeasureResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableMeasureEarFeatRespBase>>()
    override val necHearableEarFeatureMeasureResponse = _necHearableEarFeatureMeasureResponse
    private val _necHearableEarFeatureServerSendResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableFeatSendRespBase>>()
    override val necHearableEarFeatureServerSendResponse = _necHearableEarFeatureServerSendResponse
    private val _sensorDataPrepResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableSimpleBase>>()
    override val sensorDataPrepResponse = _sensorDataPrepResponse
    private val _sensorDataSendResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableMqttBase>>()
    override val sensorDataSendResponse = _sensorDataSendResponse
    private val _sensorDataStopSendResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableMqttBase>>()
    override val sensorDataStopSendResponse = _sensorDataStopSendResponse
    private val _necHearableAuthSubscribeResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableAuthSubscribeBase>>()
    override val necHearableAuthSubscribeResponse = _necHearableAuthSubscribeResponse
    private val _necHearableVoiceMemoRecordResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableVoiceMemoRecordRespBase>>()
    override val necHearableVoiceMemoRecordResponse = _necHearableVoiceMemoRecordResponse
    private val _necHearableVoiceMemoUploadResponse =
        MutableLiveData<SingleLiveDataEvent<NecHearableVoiceMemoUploadRespBase>>()
    override val necHearableVoiceMemoUploadResponse = _necHearableVoiceMemoUploadResponse

    private val weakRefContext = WeakReference(context)
    private lateinit var service: IHearableService
    override val edgeId =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    private lateinit var hearableMqttRequest: IHearableMqttRequestSender
    private lateinit var property: MqttServerProperty
    private lateinit var sensorManager: IHearableSensorManager
    // Bluetooth headset connection, as per NEC SDK v1.0.2 sample app
//    private val bluetoothCtrl = BluetoothCtrl(app.applicationContext)

    val hearableRegistVoice: IHearableRegistVoice = HearableRegistVoice()
    // Will hit "success" callback even after cancellation so need some state tracking to emit correct observable type
    private var isVoiceMemoRecordCancelled = false

    private val filesDir = context.filesDir.path
    private var necEndpoint: String? = null
    private var certification: String? = null
    private var secretkey: String? = null

    private val hearableRegistVoiceListener = object : IHearableVoiceListener {
        override fun onFinishRegist(didSucceed: Boolean) {
            ZepLog.d(TAG, "onFinishRegist(): $didSucceed")
            if (didSucceed) {
                _necHearableVoiceMemoUploadResponse.postValue(
                    SingleLiveDataEvent(
                        NecHearableVoiceMemoUploadSuccess()
                    )
                )
            } else {
                _necHearableVoiceMemoUploadResponse.postValue(
                    SingleLiveDataEvent(
                        NecHearableVoiceMemoUploadFail()
                    )
                )
            }
        }

        override fun onFinishGetVoiceFile(didSucceed: Boolean) {
            ZepLog.d(TAG, "onFinishGetVoiceFile(): $didSucceed")
        }

        override fun onFinishGetVoiceMeta(responseCode: Int, responseBody: String) {
            ZepLog.d(
                TAG,
                "onFinishGetVoiceMeta(): responseCode: code: $responseCode, response: $responseBody"
            )
        }

        /**
         * Empirically this will be triggered
         *  - when local recording successfully completed
         *  - when local recording was cancelled
         */
        override fun onFinishRecord() {
            if (!isVoiceMemoRecordCancelled) {
                _necHearableVoiceMemoRecordResponse.postValue(
                    SingleLiveDataEvent(
                        NecHearableVoiceMemoRecordSuccess()
                    )
                )
            } else {
                ZepLog.i(TAG, "onFinishRecord(): voice memo recording cancelled!")
            }
        }

        override fun onError(errorCode: Int) {
            ZepLog.e(
                TAG,
                "onError(): ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                    errorCode
                )} ($errorCode)"
            )
        }
    }

    private fun setEdgeId() {
        try {
            service.setEdgeId(edgeId)
        } catch (e: Exception) {
            throw java.lang.AssertionError("${weakRefContext.get()?.resources?.getString(R.string.error_edge_id_set_fail)}: setEdgeId(): ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}")
        }
    }

    override fun startService(context: Context) {
        service = HearableService.startService(context)
        necEndpoint = assetsTextFileToString(context, R.raw.nec_endpoint)
        if (necEndpoint == null) {
            throw java.lang.AssertionError(context.resources.getString(R.string.error_missing_nec_endpoint_file))
        }
        certification = assetsFileToString(context, R.raw.devicecertandcacertcrt)
        if (certification == null) {
            throw java.lang.AssertionError(context.resources.getString(R.string.error_missing_nec_certification_file))
        }
        secretkey = assetsFileToString(context, R.raw.devicecertkey)
        if (secretkey == null) {
            throw java.lang.AssertionError(context.resources.getString(R.string.error_missing_nec_secret_key_file))
        }
        ZepLog.d(TAG, "init: edge ID: |$edgeId|")
        // Order crucial, edge ID will throw if attempt to set after service.buildMqttPublishSender()
        setEdgeId()
        hearableMqttRequest = service.buildMqttPublishSender()
        sensorManager = service.buildSensorManager()
        property = hearableMqttRequest.buildEntity(context) as MqttServerProperty
        hearableRegistVoice.registHearableVoiceListener(hearableRegistVoiceListener)
    }

    override fun setMqttParameter() {
        try {
            property.endPoint = necEndpoint
            property.keystorePath = filesDir
            hearableMqttRequest.setMqttParameter(property, certification, secretkey,
                object : IHearableResultListener<IHearableMqttResponseEntity> {
                    override fun success(response: IHearableMqttResponseEntity) {
                        val isOK = response.status == NecHearableSDKConstants.NEC_HEARABLE_STATUS_OK
                        var msg = response.recvStr ?: ""
                        if (!isOK) {
                            msg = "WARNING: non-OK status ${response.status}: |$msg|"
                            LogUtils.e(
                                TAG,
                                "setMqttParameter()::success: $msg"
                            )
                        }
                        _mqttInitStatus.postValue(
                            NecHearableMqttSuccess(
                                isOK = isOK,
                                status = response.status,
                                msg = msg
                            )
                        )
                    }

                    override fun failed(errorCode: Int, e: Exception) {
                        val msg =
                            "ERROR (${e.javaClass.simpleName}): code ${NecHearableSDKConstants.bluetoothLeErrorCodesHumanReadableMap[errorCode]} " +
                                    "(0x${Integer.toHexString(errorCode)}, $errorCode): ${e.localizedMessage}"
                        _mqttInitStatus.postValue(
                            NecHearableMqttFail(
                                status = errorCode,
                                msg = msg
                            )
                        )
                        // Have observed that "failed()" may be invoked with no apparent relation to calling setMqttParameter()
                        _necHearableSdkError.postValue(
                            NecHearableSdkError(
                                msg
                            )
                        )
                        LogUtils.e(TAG, "setMqttParameter()::failed: $msg")
                    }
                })
        } catch (e: Exception) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _mqttInitStatus.postValue(
                NecHearableMqttFail(
                    status = -1,
                    msg = msg
                )
            )
            LogUtils.e(
                TAG,
                "setMqttParameter(): $msg"
            )
        }
    }

    override fun setWearerId(wearerId: String) {
        service.setUserId(wearerId)
    }

    override fun connectBleToExplicitMacAddr(macAddr: String) {
        try {
            service.connectBle(macAddr, object : IHearableResultListener<Boolean> {
                override fun success(response: Boolean) {
                    var msg = ""
                    if (!response) {
                        msg = "BLE connect: WARNING: \"success\" with problem!"
                        LogUtils.e(TAG, "startSendSensorData()::success(): $msg")
                    }
                    _bluetoothLeConnected.postValue(
                        NecHearableSimpleSuccess(
                            isOK = response,
                            msg = msg
                        )
                    )
                }

                override fun failed(errorCode: Int, e: Exception) {
                    val msg = "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
                    _bluetoothLeConnected.postValue(
                        NecHearableSimpleFailure(
                            errorCode = errorCode,
                            msg = msg
                        )
                    )
                    LogUtils.e(
                        TAG,
                        "connectBleToExplicitMacAddr()::failed: $msg"
                    )
                }
            })
        } catch (e: Exception) {
            val msg = "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _bluetoothLeConnected.postValue(NecHearableSimpleFailure(errorCode = -1, msg = msg))
            LogUtils.e(
                TAG,
                "connectBleToExplicitMacAddr()::failed: $msg"
            )
        }
    }

    /**
     * One-stop shopping to restore sane voiceMemo recording config, possibly after cancellation etc
     */
    private fun initVoiceMemo(wearerId: String) {
        setWearerId(wearerId)
        isVoiceMemoRecordCancelled = false
        hearableRegistVoice.setVoiceSend(true)
    }

    override fun voiceMemoPrepareRecord(
        context: Context,
        wearerId: String,
        autoStopDurationSecs: Double,
        coroutineScope: CoroutineScope
    ) {
        initVoiceMemo(wearerId)
        service.connectSco(object : IHearableResultListener<Boolean> {
            override fun success(isOK: Boolean) {
                ZepLog.d(TAG, "IHearableResultListener::success")
                if (isOK) {
                    ZepLog.d(TAG, "IHearableResultListener::success(): start recording...")
                    coroutineScope.launch {
                        voiceMemoStartRecording(
                            context,
                            autoStopDurationSecs
                        )
                    }
                } else {
                    ZepLog.e(TAG, "IHearableResultListener::success(): ERROR! (connectSco Error)")
                }
            }

            override fun failed(i: Int, e: Exception?) {
            }
        })
    }

    override fun voiceMemoStartRecording(context: Context, autoStopDurationSecs: Double) {
        ZepLog.d(TAG, "voiceMemoStartRecording()")
        val normalizeRecording = true
        val doAutoStop = true
        val automaticRecordStopSilentLevel = 5000.0
        // Note: if microphone permission not granted, this call will crash e.g.
        //      java.lang.IllegalStateException: startRecording() called on an uninitialized AudioRecord.
        var didRecordOK = false
        try {
            hearableRegistVoice.recordVoice(
                context, normalizeRecording,
                doAutoStop, automaticRecordStopSilentLevel,
                autoStopDurationSecs
            )
            didRecordOK = true
        } catch (e: Exception) {
            ZepLog.e(
                TAG,
                "voiceMemoStartRecording(): ERROR (${e.javaClass.simpleName}), possibly microphone permission not granted: ${e.localizedMessage}"
            )
        }
        // Only emit failures here; successful recording emits via callback listener elsewhere
        if (!didRecordOK) {
            _necHearableVoiceMemoRecordResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableVoiceMemoRecordFail()
                )
            )
        }
    }

    override fun voiceMemoStopRecording() {
        ZepLog.d(TAG, "voiceMemoStopRecording()")
        hearableRegistVoice.stopVoice()
        service.disconnectSco(object : IHearableResultListener<Boolean> {
            override fun success(isOK: Boolean?) {
                ZepLog.d(
                    TAG,
                    "voiceMemoStopRecording(): IHearableResultListener::success: SCO Disconnected"
                )
            }

            override fun failed(i: Int, e: Exception) {
                ZepLog.e(
                    TAG,
                    "voiceMemoStopRecording(): IHearableResultListener::failed(): ERROR (${e.javaClass.simpleName}): SCO Disconnect error: $i, ${e.localizedMessage}"
                )
            }
        })
    }

    override fun voiceMemoCancelRecording() {
        ZepLog.d(TAG, "voiceMemoCancelRecording()")
        isVoiceMemoRecordCancelled = true
        hearableRegistVoice.setVoiceSend(false)
        voiceMemoStopRecording()
    }

    override fun requestWearerId() {
        try {
            hearableMqttRequest.requestWearerId(object :
                IHearableResultListener<IHearableMqttResponseEntity> {
                override fun success(response: IHearableMqttResponseEntity) {
                    var msg = response.recvStr ?: ""
                    val isOK = response.recvStr != null
                    if (isOK) {
                        val respObj = NecHearableWearerIdSuccess.fromJson(response.recvStr)
                        _necWearerIdResponse.postValue(SingleLiveDataEvent(respObj as NecHearableWearerIdRespBase))
                    } else {
                        msg =
                            "ERROR : code: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                                response.status
                            ) ?: "Unknown error"} (${response.status}) |$msg|"
                        _necWearerIdResponse.postValue(
                            SingleLiveDataEvent(
                                NecHearableWearerIdFail(response.status, msg)
                            )
                        )
                        LogUtils.e(
                            TAG,
                            "requestWearerId()::success: ERROR! status: ${response.status}, recvStr: ${response.recvStr}"
                        )
                    }
                }

                override fun failed(statusCode: Int, e: Exception) {
                    val msg =
                        "ERROR (${e.javaClass.simpleName}): code: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                            statusCode
                        )} ($statusCode): ${e.localizedMessage}"
                    _necWearerIdResponse.postValue(
                        SingleLiveDataEvent(
                            NecHearableWearerIdFail(statusCode, msg)
                        )
                    )
                    LogUtils.e(TAG, "requestWearerId()::failed: $msg")
                }
            })
        } catch (e: Exception) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _necWearerIdResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableWearerIdFail(-1, msg)
                )
            )
            LogUtils.e(TAG, "requestWearerId(): $msg")
        }
    }

    override fun attemptMeasureEarFeature() {
        try {
            sensorManager.startFeature(object : IHearableResultListener<Boolean> {
                override fun success(response: Boolean?) {
                    _necHearableEarFeatureMeasureResponse.postValue(
                        SingleLiveDataEvent(
                            NecHearableMeasureEarFeatSuccess(true)
                        )
                    )
                    LogUtils.d(TAG, "response: $response")
                }

                override fun failed(errorCode: Int, e: Exception) {
                    val msg =
                        "ERROR (${e.javaClass.simpleName}): code: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                            errorCode
                        )} (0x${Integer.toHexString(errorCode)}, $errorCode): ${e.localizedMessage}"
                    _necHearableEarFeatureMeasureResponse.postValue(
                        SingleLiveDataEvent(
                            NecHearableMeasureEarFeatFail(false, msg)
                        )
                    )
                    LogUtils.e(TAG, "attemptMeasureEarFeature()::failed(): $msg")
                }
            })
        } catch (e: IllegalArgumentException) {
            LogUtils.e(
                TAG,
                "attemptMeasureEarFeature(): ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            )
        } catch (e: Exception) {
            LogUtils.e(
                TAG,
                "attemptMeasureEarFeature(): ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            )
        }
    }

    /**
     * Attempt to convert response to sending ear "feature" to server to appropriate object type
     */
    private fun earFeatureSendResponseJsonObj(json: String): NecHearableFeatSendRespBase? {
        var respObj: NecHearableFeatSendRespBase? = null
        try {
            respObj = NecHearableFeatSuccessRegisterIncomplete.fromJson(json)
        } catch (e: com.squareup.moshi.JsonDataException) {
            try {
                respObj = NecHearableFeatSendError.fromJson(json)
            } catch (e: com.squareup.moshi.JsonDataException) {
                try {
                    respObj = NecHearableFeatSuccessRegisterComplete.fromJson(json)
                } catch (e: com.squareup.moshi.JsonDataException) {
                    ZepLog.e(
                        TAG,
                        "startToSendFeature()::success: WARNING! failed to convert |$json| to object! "
                    )
                }
            }
        }
        return respObj
    }

    // NEC SDK "magic numbers" given meaningful names
    private val EAR_FEATURE_TYPE_EARPIECE_INSERTED_IN_EAR = 0
    private val EAR_FEATURE_TYPE_EARPIECE_NOT_INSERTED_NOT_BLOCKED = 1
    private val EAR_FEATURE_TYPE_EARPIECE_NOT_INSERTED_BLOCKED_WITH_FINGER = 2
    private val EAR_FEATURE_TYPE = EAR_FEATURE_TYPE_EARPIECE_INSERTED_IN_EAR
    /**
     * Answer from NEC 16 Dec 2019:
     *     Wait until attemptMeasureEarFeature() responds, and then call attemptSendEarFeatureToServer()
     */
    override fun attemptSendEarFeatureToServer() {
        try {
            hearableMqttRequest.startToSendFeature(
                EAR_FEATURE_TYPE,
                object : IHearableResultListener<IHearableMqttResponseEntity> {
                    override fun success(response: IHearableMqttResponseEntity) {
                        LogUtils.d(
                            TAG,
                            "startToSendFeature()::success: status: ${response.status}, recvStr: ${response.recvStr}"
                        )
                        val isOK = response.recvStr != null
                        if (isOK) {
                            earFeatureSendResponseJsonObj(response.recvStr)?.apply {
                                _necHearableEarFeatureServerSendResponse.postValue(
                                    SingleLiveDataEvent(this)
                                )
                            }
                        } else {
                            val msg = "WARNING: status: ${response.status}"
                            _necHearableEarFeatureServerSendResponse.postValue(
                                SingleLiveDataEvent(
                                    NecHearableFeatSendFail(
                                        status = response.status,
                                        message = msg
                                    )
                                )
                            )
                            LogUtils.e(
                                TAG,
                                "startToSendFeature()::success: $msg"
                            )
                        }
                    }

                    override fun failed(statusCode: Int, e: Exception) {
                        val msg =
                            "ERROR (${e.javaClass.simpleName}): code: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                                statusCode
                            )} (0x${Integer.toHexString(statusCode)}, $statusCode): ${e.localizedMessage}"
                        _necHearableEarFeatureServerSendResponse.postValue(
                            SingleLiveDataEvent(
                                NecHearableFeatSendFail(
                                    status = statusCode,
                                    message = msg
                                )
                            )
                        )
                        LogUtils.e(TAG, "startToSendFeature()::failed: $msg")
                    }
                })
        } catch (e: IllegalArgumentException) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _necHearableEarFeatureServerSendResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableFeatSendFail(
                        status = -1,
                        message = msg
                    )
                )
            )
            LogUtils.e(
                TAG,
                "startToSendFeature(): $msg"
            )
        } catch (e: Exception) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _necHearableEarFeatureServerSendResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableFeatSendFail(
                        status = -1,
                        message = msg
                    )
                )
            )
            LogUtils.e(
                TAG,
                "startToSendFeature(): $msg"
            )
        }
    }

    /**
     * Note: this is expected to usually fail due to "Hearable busy" error
     */
    override fun disconnectBle() {
        try {
            service.disconnectBle(object : IHearableResultListener<Boolean> {
                override fun success(isOK: Boolean) {
                    var msg = ""
                    if (!isOK) {
                        msg = "BLE disconnect: WARNING: \"success\" with problem!"
                        LogUtils.e(TAG, "disconnectBle()::success(): $msg")
                    }
                    _bluetoothLeDisconnected.postValue(
                        NecHearableSimpleSuccess(
                            isOK = isOK,
                            msg = msg
                        )
                    )
                }

                override fun failed(errorCode: Int, e: Exception) {
                    val msg =
                        "ERROR (${e.javaClass.simpleName}): ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                            errorCode
                        )} ($errorCode) ${e.localizedMessage}"
                    _bluetoothLeDisconnected.postValue(
                        NecHearableSimpleFailure(
                            errorCode = errorCode,
                            msg = msg
                        )
                    )
                    LogUtils.e(
                        TAG,
                        "disconnectBle()::failed: $msg"
                    )
                }
            })
        } catch (e: Exception) {
            val msg = "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _bluetoothLeDisconnected.postValue(NecHearableSimpleFailure(errorCode = -1, msg = msg))
            LogUtils.e(
                TAG,
                "disconnectBle()::failed: $msg"
            )
        }
    }

    private val statusListener: IHearableStatusListener =
        IHearableStatusListener { status ->
            _hearableStatus.postValue(status)
            LogUtils.d(
                TAG,
                "statusListener: getBleConnectDeviceName:" + status.bleConnectDeviceName
            )
            LogUtils.d(
                TAG,
                "statusListener: isBluetoothLEConnected:" + status.isBluetoothLEConnected
            )
        }

    override fun registHearableStatusListener() {
        val tag = "registHearableStatusListener()"
        try {
            service.registHearableStatusListener(statusListener)
        } catch (e: Exception) {
            LogUtils.e(TAG, "$tag: ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}")
        }
    }

    override fun requestNotifyCurrentStatus() {
        service.requestNotifyCurrentStatus()
    }

    override fun stopService() {
        service.stopService()
    }

    override fun unregisterHearableStatusListener() {
        val tag = "unregisterHearableStatusListener()"
        try {
            service.unregistHearableStatusListener(statusListener)
        } catch (e: Exception) {
            LogUtils.e(TAG, "$tag: ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}")
        }
    }

    // EmfPosition
    override fun cancelSubscribe() {
        hearableMqttRequest.cancelSubscribe()
    }

    private val LAMBDA_FUNCTION_NAME = "hearable-register-user-lm"
    private val AUTH_SUBSCRIBE_QOS = 0
    // Conference call w/NEC, 22 Jan 2020: increase "resultNum" parameter to attempt to reduce timeouts
    // on repeated authentication attempts (as is often the case during intense development)
    private val AUTHENTICSTION_RESULT_NUM = 20

    private fun requestAuth(wearerId: String) {
        if (wearerId.isEmpty()) {
            throw AssertionError("requestAuth(): NOTICE: valid wearer ID precondition for requestAuth() (currently empty!)")
        }
        val topicList = arrayOf("hearablepf/authentication/$edgeId/res")
        val param =
            """{"FunctionName":"$LAMBDA_FUNCTION_NAME" , "Payload":{"wearerId":"$wearerId","topic":"hearablepf\/authentication\/$edgeId\/res"}}"""
        hearableMqttRequest.requestSubscribe(
            topicList,
            AUTH_SUBSCRIBE_QOS,
            object : IHearableResultListener<IHearableMqttResponseEntity> {
                override fun success(iHearableMqttResponseEntity: IHearableMqttResponseEntity) {
                    val result = iHearableMqttResponseEntity.recvStr
                    val jsonObj: NecHearableAuthSubscribeSuccess? =
                        NecHearableAuthSubscribeSuccess.fromJson(result)
                    _necHearableAuthSubscribeResponse.postValue(SingleLiveDataEvent(jsonObj as NecHearableAuthSubscribeBase))
                    ZepLog.d(TAG, "requestAuth()::success: jsonObj: |${jsonObj.result}|")
                    hearableMqttRequest.cancelSubscribe()
                }

                override fun failed(errorCode: Int, e: Exception) {
                    val msg =
                        "ERROR (${e.javaClass.simpleName}): code ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                            errorCode
                        )} (0x${Integer.toHexString(errorCode)}, $errorCode), msg: ${e.localizedMessage}"
                    ZepLog.e(TAG, "requestAuth()::failed: $msg")
                    val jsonObj = NecHearableAuthSubscribeFail(errorCode, msg)
                    _necHearableAuthSubscribeResponse.postValue(SingleLiveDataEvent(jsonObj as NecHearableAuthSubscribeBase))
                    hearableMqttRequest.cancelSubscribe()
                }
            })
        hearableMqttRequest.requestAuth(param, AUTHENTICSTION_RESULT_NUM)
    }

    @kotlinx.coroutines.InternalCoroutinesApi
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    override fun registerEarTimeoutFlow(): ColdFlowWTimeout {
        return ColdFlowWTimeout(
            NecHearableSDKConstants.requestAuthTimeoutSecs,
            "registerEarTimeoutFlow",
            { })
        { }
    }

    @kotlinx.coroutines.InternalCoroutinesApi
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    override fun requestAuthFlow(wearerId: String): ColdFlowWTimeout {
        return ColdFlowWTimeout(
            NecHearableSDKConstants.requestAuthTimeoutSecs,
            "requestAuthFlow",
            { requestAuth(wearerId) })
        { hearableMqttRequest.cancelSubscribe() }
    }

    private val SENSOR_DATA_SEND_INTERVAL_MILLISEC = 20 * 1000L
    private val SENSOR_DATA_SEND_INDIRECT = 0

    private fun startSendSensorDataToServer() {
        try {
            hearableMqttRequest.startToSendData(
                SENSOR_DATA_SEND_INTERVAL_MILLISEC,
                SENSOR_DATA_SEND_INDIRECT,
                object : IHearableResultListener<IHearableMqttResponseEntity> {
                    override fun success(response: IHearableMqttResponseEntity) {
                        var msg = response.recvStr ?: ""
                        val isOK = response.status == NecHearableSDKConstants.NEC_HEARABLE_STATUS_OK
                        if (!isOK) {
                            msg =
                                "WARNING: success response w/error! status: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                                    response.status
                                )} (${response.status}) |$msg|"
                            LogUtils.e(
                                TAG,
                                "startSendSensorDataToServer()::success(): $msg"
                            )
                        }
                        _sensorDataSendResponse.postValue(
                            SingleLiveDataEvent(
                                NecHearableMqttSuccess(
                                    isOK = isOK,
                                    status = response.status,
                                    msg = msg
                                )
                            )
                        )
                    }

                    override fun failed(errorCode: Int, e: Exception) {
                        val msg =
                            "ERROR (${e.javaClass.simpleName}): code ${NecHearableSDKConstants.bluetoothLeErrorCodesHumanReadableMap[errorCode]} " +
                                    "(0x${Integer.toHexString(errorCode)}, $errorCode), msg ${e.localizedMessage}"
                        _sensorDataSendResponse.postValue(
                            SingleLiveDataEvent(
                                NecHearableMqttFail(
                                    status = errorCode,
                                    msg = msg
                                )
                            )
                        )
                        LogUtils.e(
                            TAG,
                            "startSendSensorDataToServer()::failed: $msg"
                        )
                    }
                })
        } catch (e: IllegalArgumentException) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _sensorDataSendResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableMqttFail(
                        status = -1,
                        msg = msg
                    )
                )
            )
            LogUtils.e(TAG, msg)
        } catch (e: Exception) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _sensorDataSendResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableMqttFail(
                        status = -1,
                        msg = msg
                    )
                )
            )
            LogUtils.e(TAG, msg)
        }
    }

    override fun startSendSensorData(doSendNineAxisSensor: Boolean, doSendTempSensor: Boolean) {
        val dataTypes: MutableList<Int> = ArrayList()
        if (doSendNineAxisSensor) {
            dataTypes.add(HearableSensorManager.DATA_TYPE_NINE_AXIS)
        }
        if (doSendTempSensor) {
            dataTypes.add(HearableSensorManager.DATA_TYPE_TEMP)
        }
        if (dataTypes.isNotEmpty()) {
            try {
                sensorManager.startSensor(object : IHearableResultListener<Boolean> {
                    override fun success(didSucceed: Boolean) {
                        if (didSucceed) {
                            _sensorDataPrepResponse.postValue(
                                SingleLiveDataEvent(
                                    NecHearableSimpleSuccess(isOK = true, msg = "")
                                )
                            )
                            startSendSensorDataToServer()
                        } else {
                            val msg = "WARNING: sensor prep failure!"
                            _sensorDataPrepResponse.postValue(
                                SingleLiveDataEvent(
                                    NecHearableSimpleSuccess(isOK = false, msg = msg)
                                )
                            )
                            LogUtils.e(TAG, "startSendSensorData()::success(): $msg")
                        }
                    }

                    override fun failed(errorCode: Int, e: Exception) {
                        val msg =
                            "ERROR (${e.javaClass.simpleName}): code: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                                errorCode
                            )} ($errorCode): ${e.localizedMessage}"
                        _sensorDataPrepResponse.postValue(
                            SingleLiveDataEvent(
                                NecHearableSimpleFailure(errorCode = errorCode, msg = msg)
                            )
                        )
                        LogUtils.e(
                            TAG,
                            "startSendSensorData()::failed: $msg"
                        )
                    }
                }, dataTypes)
            } catch (e: Exception) {
                val msg = "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
                _sensorDataPrepResponse.postValue(
                    SingleLiveDataEvent(
                        NecHearableSimpleFailure(
                            errorCode = -1,
                            msg = msg
                        )
                    )
                )
                LogUtils.e(
                    TAG,
                    "startSendSensorData(): $msg"
                )
            }
        } else {
            val msg = "WARNING: no sensor data specified, nothing to send!"
            _sensorDataPrepResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableSimpleFailure(
                        errorCode = -1,
                        msg = msg
                    )
                )
            )
            ZepLog.i(TAG, "startSendSensorData(): $msg")
        }
    }

    override fun stopSendSensorData() {
        try {
            hearableMqttRequest.stopToSendData(object :
                IHearableResultListener<IHearableMqttResponseEntity> {
                override fun success(response: IHearableMqttResponseEntity) {
                    var msg = response.recvStr ?: ""
                    val isOK = response.status == NecHearableSDKConstants.NEC_HEARABLE_STATUS_OK
                    if (!isOK) {
                        msg =
                            "WARNING: success response w/error! status: ${NecHearableSDKConstants.necHearableSDKErrorCodesHumanReadableMap.get(
                                response.status
                            )} (${response.status}) |$msg|"
                        LogUtils.e(
                            TAG,
                            "stopSendSensorData()::success(): $msg"
                        )
                    }
                    _sensorDataStopSendResponse.postValue(
                        SingleLiveDataEvent(
                            NecHearableMqttSuccess(
                                isOK = isOK,
                                status = response.status,
                                msg = msg
                            )
                        )
                    )
                }

                override fun failed(errorCode: Int, e: Exception) {
                    val msg =
                        "ERROR (${e.javaClass.simpleName}): code ${NecHearableSDKConstants.bluetoothLeErrorCodesHumanReadableMap[errorCode]} " +
                                "(0x${Integer.toHexString(errorCode)}, $errorCode), msg ${e.localizedMessage}"
                    _sensorDataStopSendResponse.postValue(
                        SingleLiveDataEvent(
                            NecHearableMqttFail(
                                status = errorCode,
                                msg = msg
                            )
                        )
                    )
                    LogUtils.e(
                        TAG,
                        "stopSendSensorData()::failed: $msg"
                    )
                }
            })
        } catch (e: Exception) {
            val msg =
                "ERROR (${e.javaClass.simpleName}): ${e.localizedMessage}"
            _sensorDataStopSendResponse.postValue(
                SingleLiveDataEvent(
                    NecHearableMqttFail(
                        status = -1,
                        msg = msg
                    )
                )
            )
            LogUtils.e(
                TAG,
                "stopSendSensorData(): $msg"
            )
        }
    }

    private fun assetsTextFileToString(context: Context, file: Int): String? {
        val ins = context.resources.openRawResource(file)
        return try {
            val reader = BufferedReader(InputStreamReader(ins))
            val lines = mutableListOf<String>()
            var line: String? = reader.readLine()
            while (line != null) {
                lines.add(line.trim())
                line = reader.readLine()
            }
            lines.joinToString("\n")
        } catch (ie: IOException) {
            LogUtils.e(TAG, "assetsFileToString(): ERROR: $file : Read Error")
            null
        }
    }

    private fun assetsFileToString(context: Context, file: Int): String? {
        val ins = context.resources.openRawResource(file)
        return try {
            val buff = ByteArray(4 * 1024)
            val readSize: Int
            readSize = ins.read(buff)
            LogUtils.d(
                TAG,
                "assetsFileToString(): file read length:$readSize"
            )
            String(buff)
        } catch (ie: IOException) {
            LogUtils.e(TAG, "assetsFileToString(): ERROR: $file : Read Error")
            null
        }
    }

    companion object {
        private val TAG = NecHearableSdkWrapper::class.java.simpleName

        // From NEC 17 Dec 2019: "QoS is MQTT QoS. 0 or 1 can be set. Use 0 for this project"
        private const val MQTT_QOS = 0

        // From NEC 17 Dec 2019: "AUTH_RESULTS_NUMBER is the number of wearerId and score combinations.
        // Returns the specified number and combination in descending order of score."
        private const val AUTH_RESULTS_NUMBER = 3
    }
}
