package jp.co.zeppelin.nec.hearable.necsdkwrapper.contract

import android.content.Context
import androidx.lifecycle.LiveData
import com.nec.Android.hearable.IHearableStatus
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.model.NecHearableEventBase
import jp.co.zeppelin.nec.hearable.necsdkwrapper.coro.ColdFlowWTimeout
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.*
import kotlinx.coroutines.CoroutineScope

/**
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface INecSdkContract {
    /**
     * Unique android device ID required by NEC backend servers
     */
    val edgeId: String
    /**
     * Observable for hearable status changes
     */
    val hearableStatus: LiveData<IHearableStatus>
    /**
     * Observable response to attempt to init NEC Hearable SDK
     */
    val mqttInitStatus: LiveData<NecHearableMqttBase>
    /**
     * Observable response to attempt to connect to NEC Hearable device via Bluetooth LE
     */
    val bluetoothLeConnected: LiveData<NecHearableSimpleBase>
    /**
     * Observable response to attempt to connect to NEC Hearable device via Bluetooth LE
     */
    val bluetoothLeDisconnected: LiveData<NecHearableSimpleBase>
    /**
     * Observable response to request for new Wearer ID from NEC backend servers
     */
    val necWearerIdResponse: LiveData<SingleLiveDataEvent<NecHearableWearerIdRespBase>>
    /**
     * Observable response to device-local attempt to measure inner ear "feature" via digital sound
     * playback from NEC hearable device
     */
    val necHearableEarFeatureMeasureResponse: LiveData<SingleLiveDataEvent<NecHearableMeasureEarFeatRespBase>>
    /**
     * Observable response to attempt to upload inner ear "feature" to NEC backend servers
     */
    val necHearableEarFeatureServerSendResponse: LiveData<SingleLiveDataEvent<NecHearableFeatSendRespBase>>
    /**
     * Observable response to device-local request to prepare for sending hearable sensor data to backend servers
     */
    val sensorDataPrepResponse: LiveData<SingleLiveDataEvent<NecHearableSimpleBase>>
    /**
     * Observable response to request to begin sending hearable sensor data to backend servers
     */
    val sensorDataSendResponse: LiveData<SingleLiveDataEvent<NecHearableMqttBase>>
    /**
     * Observable response to request to stop sending hearable sensor data to backend servers
     */
    val sensorDataStopSendResponse: LiveData<SingleLiveDataEvent<NecHearableMqttBase>>
    /**
     * Observable response to attempt to verify/authenticate user via digital sound
     * playback from NEC hearable device
     */
    val necHearableAuthSubscribeResponse: LiveData<SingleLiveDataEvent<NecHearableAuthSubscribeBase>>
    val necHearableVoiceMemoRecordResponse: LiveData<SingleLiveDataEvent<NecHearableVoiceMemoRecordRespBase>>
    val necHearableVoiceMemoUploadResponse: LiveData<SingleLiveDataEvent<NecHearableVoiceMemoUploadRespBase>>
    /**
     * Observable for NEC SDK errors
     */
    val necHearableSdkError: LiveData<NecHearableEventBase>

    fun startService(context: Context)

    /**
     * Init the NEC Hearable SDK; nothing can be done until this succeeds
     */
    fun setMqttParameter()

    /**
     * "Wearer ID" (aka "User ID") is UUID obtained from NEC backend servers
     */
    fun requestWearerId()

    /**
     * Attempt to measure inner ear "feature" via digital sound playback from NEC Hearable earpiece
     */
    fun attemptMeasureEarFeature()

    /**
     * Attempt to send measured inner ear "feature" to NEC backend servers
     */
    fun attemptSendEarFeatureToServer()

    /**
     * Simple timeout for register ear feature, for scenarios where no callback will arrive e.g.
     *  - hearable locks up (e.g. on Samsung handset) so no sound emitted
     *
     * @return observable (kt coroutine cold "Flow") with timeout
     */
    @kotlinx.coroutines.InternalCoroutinesApi
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun registerEarTimeoutFlow(): ColdFlowWTimeout

    /**
     * Attempt to verify/authenticate user by checking inner ear "feature" via digital sound playback
     * from NEC Hearable earpiece
     *
     * @return observable (kt coroutine cold "Flow") with timeout
     */
    @kotlinx.coroutines.InternalCoroutinesApi
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun requestAuthFlow(wearerId: String): ColdFlowWTimeout

    // Start sensor data stream
    fun startSendSensorData(doSendNineAxisSensor: Boolean, doSendTempSensor: Boolean)

    fun stopSendSensorData()

    fun registHearableStatusListener()
    fun unregisterHearableStatusListener()

    fun requestNotifyCurrentStatus()

    fun cancelSubscribe()
    fun disconnectBle()
    fun stopService()

    interface INecHearableSdkImplContract : INecSdkContract {
        fun setWearerId(wearerId: String)

        fun connectBleToExplicitMacAddr(macAddr: String)

        fun voiceMemoPrepareRecord(
            context: Context,
            wearerId: String,
            autoStopDurationSecs: Double,
            coroutineScope: CoroutineScope
        )

        fun voiceMemoStartRecording(context: Context, autoStopDurationSecs: Double)
        fun voiceMemoStopRecording()
        fun voiceMemoCancelRecording()
    }

    interface INecHearableSdkUIContract : INecSdkContract {
        var wearerId: String

        fun setSdkWearerId()

        @kotlinx.coroutines.InternalCoroutinesApi
        @kotlinx.coroutines.ExperimentalCoroutinesApi
        fun attemptConnectBluetoothLeFlow(): ColdFlowWTimeout

        @kotlinx.coroutines.InternalCoroutinesApi
        @kotlinx.coroutines.ExperimentalCoroutinesApi
        fun attemptInitMqttFlow(): ColdFlowWTimeout
    }
}
