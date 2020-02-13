package jp.co.zeppelin.nec.hearable.ui.home

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.ThreadHelper
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.BluetoothTargetDeviceFound
import jp.co.zeppelin.nec.hearable.helper.AlertHelper
import jp.co.zeppelin.nec.hearable.helper.BatteryHelper
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.*
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import jp.co.zeppelin.nec.hearable.voicememo.model.VoiceMemoRecordProgress
import jp.co.zeppelin.nec.hearable.voicememo.model.VoiceMemoRecordTimeout
import kotlinx.android.synthetic.main.frag_home.*

/**
 * User "home" screen, displayed after registration and/or authentication complete
 *
 * Some complexity in here due to possibility of hearable being disconnected while
 * this screen displayed, and goal of keeping screen "live" based on hearable
 * connection status
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class HomeFrag : BaseVmFrag() {
    private val TAG = HomeFrag::class.java.simpleName

    lateinit var rootView: View
    // set via ACL connected observable; "true" is reasonable when navigating to screen
    private var isTargetDeviceBluetoothClassicConnected = true
    // Avoid "IllegalArgumentException: navigation destination ... is unknown to this NavController"
    private var haveShownTargetDeviceDisconnectedDialog = false
    // set via BLE connected observable; "true" is reasonable when navigating to screen
    private var isBLEConnected = true
    private var isBLEConnectedDebounce = isBLEConnected
    private var isVoiceMemoPanelVisible = false

    var recordSeconds = 7L
    var highlightSeconds = 3L
    var didUserStopRecording = false
    var didUserCancelRecording = false

    var COLOR_NEC_GREEN = Color.DKGRAY

    // For testing, typ. procedure
    //  - set TEST_BYPASS_MICROPHONE_PERMISSION true
    //  - in device system settings, revoke microphone permission for this app
    //  - relaunch, connect to hearable, authenticate, and get to "Home" screen
    //  - attempt to record voiceMemo; will fail due to missing audio permission
    val TEST_BYPASS_MICROPHONE_PERMISSION = false

    private enum class SendSensorDataStatus {
        Init,
        Success,
        Error
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.frag_home, container, false)
        return rootView
    }

    private fun crossFadeBatteryLevel(batteryPct: Int = -1) {
        var rawString = resources.getString(R.string.battery_unknown)
        var statusColor = Color.RED
        if (batteryPct > -1) {
            imageViewBatteryLevel?.setBackgroundResource(
                BatteryHelper.batteryDrawableForPct(
                    batteryPct
                )
            )
            rawString =
                resources.getString(R.string.home_format_battery, batteryPct)
            statusColor = ContextCompat.getColor(context!!, R.color.nec_green)
            startFadeInFor(imageViewBatteryLevel)
            startFadeOutFor(imageViewBatteryUnknown)
        } else {
            startFadeInFor(imageViewBatteryUnknown)
            val objAnim = startFadeOutFor(imageViewBatteryLevel)
            objAnim?.addUpdateListener { valueAnimator ->
                if (valueAnimator.animatedFraction > 0.99) {
                    imageViewBatteryLevel?.setBackgroundResource(R.drawable.battery_unknown)
                }
            }
        }
        val spannable = SpannableString(rawString)
        spannable.setSpan(
            ForegroundColorSpan(statusColor),
            0, rawString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewLabelBattery?.text = spannable
    }

    private fun initTimer() {
        val rawString = resources.getString(R.string.voicememo_record_init)
        val spannable = SpannableString(rawString)
        textViewRecordStatus?.text = resources.getString(R.string.voicememo_ready)
        textViewRecordTimer?.text = spannable
    }

    private fun updateTimer(tickSecs: Long) {
        val rawString = resources.getString(
            R.string.format_voicememo_record_secs,
            tickSecs,
            recordSeconds - tickSecs
        )
        val spannable = SpannableString(rawString)
        if (recordSeconds - tickSecs <= highlightSeconds) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                8, rawString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textViewRecordTimer.text = spannable
    }

    private fun startRecording() {
        lottieRecordCircle.resumeAnimation()
        val countdownTimer = viewModel.voiceMemoRecordTimer(recordSeconds)
        imageViewStopIcon.visibility = View.VISIBLE
        imageViewCheckmark.visibility = View.INVISIBLE
        viewModel.startRecording(recordSeconds.toDouble())
        countdownTimer.start().observe(viewLifecycleOwner, Observer { tick ->
            when (tick) {
                is VoiceMemoRecordProgress -> {
                    ZepLog.d(TAG, "updateVoiceMemoState:obs: recorded ${tick.recordSecs} secs...")
                    textViewRecordStatus.text = resources.getString(R.string.voicememo_recording)
                    updateTimer(tick.recordSecs)
                    if (didUserStopRecording) {
                        countdownTimer.isCancelled = true
                        markRecordingComplete()
                    } else if (didUserCancelRecording) {
                        countdownTimer.isCancelled = true
                        slideVoiceMemoPanel()
                    }
                }
                is VoiceMemoRecordTimeout -> {
                    ZepLog.i(TAG, "updateVoiceMemoState:obs: timeout!")
                    if (!didUserStopRecording) {
                        updateTimer(recordSeconds)
                        markRecordingComplete()
                    }
                }
            }
        })

    }

    private fun markRecordingComplete() {
        lottieRecordCircle.cancelAnimation()
        lottieRecordCircle.progress = 0f
        imageViewStopIcon.visibility = View.INVISIBLE
        imageViewCheckmark.visibility = View.VISIBLE
        textViewRecordStatus.text = resources.getString(R.string.voicememo_recording_complete)
        viewModel.stopRecording()
        viewModel.doActionAfterDelay(1000) {
            slideVoiceMemoPanel()
        }
    }

    private fun cancelVoiceMemoRecording() {
        didUserCancelRecording = true
    }

    private fun slideVoiceMemoPanel() {
        val slidePixels = resources.getDimension(R.dimen.voicememo_slide_panel_height)
        val objAnimSlide = if (isVoiceMemoPanelVisible) {
            ObjectAnimator.ofFloat(cardviewRecord, "translationY", -slidePixels, 0f)
        } else {
            ObjectAnimator.ofFloat(cardviewRecord, "translationY", 0f, -slidePixels)
        }
        objAnimSlide
            .setDuration(resources.getInteger(R.integer.voicememo_fade_millisec).toLong())
            .start()

        val fadeAnimResId =
            if (isVoiceMemoPanelVisible) R.anim.voicememo_fab_fadein else R.anim.voicememo_fab_fadeout
        val animFabFade: Animation =
            AnimationUtils.loadAnimation(context, fadeAnimResId)
        // Note: need to set "fillAfter" programatically, no effect in xml
        animFabFade.fillAfter = true
        fabRecordVoiceMemo?.startAnimation(animFabFade)

        // Reset timer to prevent transient re-display of previously cancelled timer
        initTimer()

        isVoiceMemoPanelVisible = !isVoiceMemoPanelVisible
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordSeconds = resources.getInteger(R.integer.voicememo_record_seconds).toLong()
        highlightSeconds = resources.getInteger(R.integer.voicememo_highlight_last_seconds).toLong()
        context?.apply {
            COLOR_NEC_GREEN = ContextCompat.getColor(this, R.color.nec_green)
        }
    }

    private fun updateSensorStatus(status: SendSensorDataStatus, rawStringResId: Int) {
        val rawString = resources.getString(rawStringResId)
        val statusColor = when (status) {
            SendSensorDataStatus.Success -> COLOR_NEC_GREEN
            SendSensorDataStatus.Error -> Color.RED
            else -> Color.GRAY
        }
        val spannable = SpannableString(rawString)
        spannable.setSpan(
            ForegroundColorSpan(statusColor),
            0, rawString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // If sensors stopping could be shutting down fragment so watch nullability
        textViewSensorStatus?.text = spannable
    }

    private fun updateDeviceId(isHearableConnected: Boolean, rawString: String) {
        ZepLog.d(TAG, "updateDeviceId(): $isHearableConnected")
        val statusColor = when (isHearableConnected) {
            true -> ContextCompat.getColor(context!!, R.color.nec_green)
            else -> Color.RED
        }
        val spannable = SpannableString(rawString)
        spannable.setSpan(
            ForegroundColorSpan(statusColor),
            0, rawString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewDeviceId?.text = spannable
    }

    private fun startSendSensorData() {
        viewModel.startSendSensorData(doSendNineAxisSensor = true, doSendTempSensor = true)
    }

    /**
     * Update UI depending on bluetooth "classic"/BLE connection statuis
     *
     * Note:
     *  - shutdown on bluetooth ACL ("classic") disconnected
     *  - (re-)start on BLE connected
     */
    private fun updateForConnectStatus() {
        if (isTargetDeviceBluetoothClassicConnected) {
            updateSensorStatus(
                status = SendSensorDataStatus.Init,
                rawStringResId = R.string.home_sensor_status_prep_init
            )
            if (isBLEConnected) {
                startSendSensorData()
                updateDeviceId(
                    isHearableConnected = isBLEConnected,
                    rawString = DeviceId.macAddressFromDeviceId(viewModel.lastTargetDeviceId())
                )
                fabRecordVoiceMemo.isEnabled = true
                fabRecordVoiceMemo.show()
            }
        } else {
            viewModel.stopSendSensorData()
            crossFadeBatteryLevel()
            updateDeviceId(
                isHearableConnected = isTargetDeviceBluetoothClassicConnected,
                rawString = resources.getString(R.string.home_device_not_connected)
            )
            fabRecordVoiceMemo.isEnabled = false
            fabRecordVoiceMemo.hide()
        }
    }

    private fun observeBluetoothAclDeviceFound() {
        viewModel.bluetoothAclConnectedStatus.observe(
            viewLifecycleOwner,
            Observer { bluetoothAclConnectStatusSgl ->
                bluetoothAclConnectStatusSgl.getContentIfNotHandled()
                    ?.let { bluetoothAclConnectStatus ->
                        isTargetDeviceBluetoothClassicConnected = false
                        var isDisconnected = true
                        when (bluetoothAclConnectStatus) {
                            is BluetoothAclConnected -> {
                                isDisconnected = false
                                if (DeviceId.macAddressFromDeviceId(viewModel.lastTargetDeviceId()) == bluetoothAclConnectStatus.macAddress) {
                                    isTargetDeviceBluetoothClassicConnected = true
                                    viewModel.startBluetoothLeDiscovery()
                                }
                            }
                            else -> {
                            }
                        }
                        if (!isTargetDeviceBluetoothClassicConnected) {
                            if (!haveShownTargetDeviceDisconnectedDialog) {
                                NavHostFragment.findNavController(this@HomeFrag)
                                    .navigate(HomeFragDirections.actionNavHomeToNavDialogHearableDisconnected())
                            }
                            haveShownTargetDeviceDisconnectedDialog = true
                            if (isDisconnected) {
                                ZepLog.i(
                                    TAG,
                                    "bluetoothAclConnectedStatus::observer: ACL DISCONNECTED!, threadId: ${ThreadHelper.threadId(
                                        Thread.currentThread()
                                    )}"
                                )
                            } else {
                                ZepLog.e(
                                    TAG,
                                    "bluetoothAclConnectedStatus::observer: ACL CONNECTED TO NON-TARGET HEARABLE!, threadId: ${ThreadHelper.threadId(
                                        Thread.currentThread()
                                    )}"
                                )
                            }
                        }
                        updateForConnectStatus()
                    }
            })
    }

    private fun observeTargetBleDeviceFound() {
        viewModel.targetLEDeviceFound.observe(viewLifecycleOwner, Observer { bluetoothMsgSingle ->
            //            bleDiscoveryflow.isCancelled = true
            bluetoothMsgSingle.getContentIfNotHandled()?.let { targetDeviceWrapper ->
                when (targetDeviceWrapper) {
                    is BluetoothTargetDeviceFound -> {
                        viewModel.targetDevice = targetDeviceWrapper.device
                        viewModel.connectBleDevice()
                        ZepLog.d(
                            TAG,
                            "targetLEDeviceFound::BluetoothTargetDeviceFound observer: ${targetDeviceWrapper.device} (thread ${ThreadHelper.threadId(
                                Thread.currentThread()
                            )})"
                        )
                    }
                    else -> {
                        ZepLog.e(
                            TAG,
                            "targetLEDeviceFound::BluetoothTargetDeviceFound observer: NOTICE: unexpected event! (thread ${ThreadHelper.threadId(
                                Thread.currentThread()
                            )})"
                        )
                    }
                }
            }
        })
    }

    private fun observeBleConnected() {
        viewModel.bluetoothLeConnectStatus.observe(viewLifecycleOwner, Observer { bleStatusSgl ->
            bleStatusSgl.getContentIfNotHandled()?.let { bleStatus ->
                val toastMsg = when (bleStatus) {
                    is BluetoothLeConnected -> {
                        // Ensure both "classic" and BLE connected
                        if (isTargetDeviceBluetoothClassicConnected) {
                            isBLEConnected = true
                            updateForConnectStatus()
                            ZepLog.d(
                                TAG,
                                "bluetoothLeConnectStatus::observer: both BT classic and BLE connected"
                            )
                        }
                        resources.getString(R.string.toast_ble_connect_success)
                    }
                    is BluetoothLeDisconnected -> {
                        isBLEConnected = false
                        if (isBLEConnected != isBLEConnectedDebounce) {
                            updateForConnectStatus()
                            ZepLog.i(
                                TAG,
                                "bluetoothLeConnectStatus::observer: BLE disconnected"
                            )
                        }
                        isBLEConnectedDebounce = isBLEConnected
                        resources.getString(R.string.toast_ble_disconnect_success)
                    }
                }
                AlertHelper.makeToast(context, toastMsg)
            }

        })
    }

    private fun observeSensorDataSendEvents() {
        viewModel.sensorDataPrepResponse.observe(viewLifecycleOwner, Observer { necSdkEvtSgl ->
            necSdkEvtSgl.getContentIfNotHandled()?.let { necSdkEvt ->
                val rawStringResId =
                    if (necSdkEvt.isOK) R.string.home_sensor_status_prep_ok else R.string.home_sensor_status_prep_error
                val status =
                    if (necSdkEvt.isOK) SendSensorDataStatus.Success else SendSensorDataStatus.Error
                updateSensorStatus(status = status, rawStringResId = rawStringResId)
                val toastMsg =
                    if (necSdkEvt.isOK) resources.getString(R.string.toast_sensor_data_send_prep_success) else resources.getString(
                        R.string.toast_sensor_data_send_prep_error
                    )
                AlertHelper.makeToast(context, toastMsg)
            }
        })

        viewModel.sensorDataSendResponse.observe(viewLifecycleOwner, Observer { mqttSdkEvtSgl ->
            mqttSdkEvtSgl.getContentIfNotHandled()?.let { mqttSdkEvt ->
                val rawStringResId =
                    if (mqttSdkEvt.isOK) R.string.home_sensor_status_send_ok else R.string.home_sensor_status_send_error
                val status =
                    if (mqttSdkEvt.isOK) SendSensorDataStatus.Success else SendSensorDataStatus.Error
                updateSensorStatus(
                    status = status,
                    rawStringResId = rawStringResId
                )
                val toastMsg =
                    if (mqttSdkEvt.isOK) resources.getString(R.string.toast_sensor_data_start_send_success) else resources.getString(
                        R.string.toast_sensor_data_start_send_error
                    )
                AlertHelper.makeToast(context, toastMsg)
            }
        })

        viewModel.sensorDataStopSendResponse.observe(viewLifecycleOwner, Observer { mqttSdkEvtSgl ->
            mqttSdkEvtSgl.getContentIfNotHandled()?.let { mqttSdkEvt ->
                val rawStringResId =
                    if (mqttSdkEvt.isOK) R.string.home_sensor_status_stop_send_ok else R.string.home_sensor_status_stop_send_error
                val status =
                    if (mqttSdkEvt.isOK) SendSensorDataStatus.Success else SendSensorDataStatus.Error
                updateSensorStatus(
                    status = status,
                    rawStringResId = rawStringResId
                )
                val toastMsg =
                    if (mqttSdkEvt.isOK) resources.getString(R.string.toast_sensor_data_stop_success) else resources.getString(
                        R.string.toast_sensor_data_stop_error
                    )
                AlertHelper.makeToast(context, toastMsg)
            }
        })
    }

    private fun observeVoiceMemoEvents() {
        viewModel.necHearableVoiceMemoRecordResponse.observe(
            viewLifecycleOwner,
            Observer { sglEvt ->
                sglEvt.getContentIfNotHandled()?.let { necHearableVoiceRecordResp ->
                    when (necHearableVoiceRecordResp) {
                        is NecHearableVoiceMemoRecordSuccess -> {
                            Snackbar.make(
                                rootView,
                                resources.getString(R.string.voicememo_snackbar_upload_in_progress),
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("Action", null).show()
                        }
                        is NecHearableVoiceMemoRecordFail -> {
                            ZepLog.e(TAG, "necHearableVoiceMemoRecordResponse::observer: fail")
                            NavHostFragment.findNavController(this@HomeFrag)
                                .navigate(HomeFragDirections.actionNavHomeToNavDialogVoiceMemoRecordError())
                        }
                    }
                }
            })

        viewModel.necHearableVoiceMemoUploadResponse.observe(
            viewLifecycleOwner,
            Observer { sglEvt ->
                sglEvt.getContentIfNotHandled()?.let { necHearableVoiceMemoUploadResp ->
                    when (necHearableVoiceMemoUploadResp) {
                        is NecHearableVoiceMemoUploadSuccess -> {
                            Snackbar.make(
                                rootView,
                                resources.getString(R.string.voicememo_snackbar_upload_complete),
                                Snackbar.LENGTH_LONG
                            )
                                .setAction("Action", null).show()
                        }
                        is NecHearableVoiceMemoUploadFail -> {
                            ZepLog.e(TAG, "necHearableVoiceMemoUploadResponse::observer: fail")
                            NavHostFragment.findNavController(this@HomeFrag)
                                .navigate(HomeFragDirections.actionNavHomeToNavDialogVoiceMemoUploadFail())
                        }
                    }
                }
            })

        viewModel.cancelRecording.observe(viewLifecycleOwner, Observer { singleEvent ->
            singleEvent.getContentIfNotHandled()?.let { _ ->
                cancelVoiceMemoRecording()
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        updateForConnectStatus()

        observeSensorDataSendEvents()

        observeBluetoothAclDeviceFound()

        observeTargetBleDeviceFound()

        observeBleConnected()

        viewModel.batteryPctLive.observe(viewLifecycleOwner, Observer { batteryPct ->
            ZepLog.d(TAG, "onActivityCreated(): batteryPctLive::observer: batteryPct: $batteryPct")
            // BLE is an oddball; can remain connected even when Bluetooth "Classic" disconnected
            if (isTargetDeviceBluetoothClassicConnected) {
                crossFadeBatteryLevel(batteryPct)
            }
        })

        fabRecordVoiceMemo.setOnClickListener {
            if (TEST_BYPASS_MICROPHONE_PERMISSION || viewModel.isPermissionMicrophoneGranted()) {
                // Unable to use animation resource files due to broken button clickability; go programmatic instead
                didUserStopRecording = false
                didUserCancelRecording = false

                slideVoiceMemoPanel()

                startRecording()
            } else {
                NavHostFragment.findNavController(this@HomeFrag)
                    .navigate(HomeFragDirections.actionNavHomeToNavExplainPermissionMicrophone())
            }
        }

        voiceMemoPauseDoneButton.setOnClickListener {
            ZepLog.e(TAG, "voiceMemoPauseDoneButton clicked...")
            didUserStopRecording = true
        }

        textViewButtonCancel.setOnClickListener {
            NavHostFragment.findNavController(this@HomeFrag)
                .navigate(HomeFragDirections.actionNavHomeToNavDialogVoicememoCancel())
        }

        observeVoiceMemoEvents()
    }

    override fun onResume() {
        super.onResume()
        crossFadeBatteryLevel()
        viewModel.startBluetoothLeDiscovery()
    }

    override fun onStop() {
        viewModel.stopSendSensorData()
        super.onStop()
    }
}
