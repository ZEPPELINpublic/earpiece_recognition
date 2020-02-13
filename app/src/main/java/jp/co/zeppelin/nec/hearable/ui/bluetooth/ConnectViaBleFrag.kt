package jp.co.zeppelin.nec.hearable.ui.bluetooth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.NecHearableSdkError
import jp.co.zeppelin.nec.hearable.domain.model.ProgressUpdateEvent
import jp.co.zeppelin.nec.hearable.domain.model.TimeoutEvent
import jp.co.zeppelin.nec.hearable.helper.AlertHelper
import jp.co.zeppelin.nec.hearable.navigation.INavigationContract
import jp.co.zeppelin.nec.hearable.necsdkwrapper.coro.ColdFlowWTimeout
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.*
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_bluetooth_discovery.*
import kotlinx.android.synthetic.main.widget_pairing_progress.*

/**
 * Screen displayed during BLE connection attempt to Hearable
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ConnectViaBleFrag : BaseLocationPermissionFrag() {
    private val TAG = ConnectViaBleFrag::class.java.simpleName

    private lateinit var attemptConnectBleFlow: ColdFlowWTimeout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_bluetooth_discovery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewLabelTitle.text = resources.getString(R.string.label_initiating_bluetooth_le_title)
        startFadeOutFor(buttonNextConnection)
        startFadeInFor(progressConnect)
    }

    private fun observeBleConnect() {
        ZepLog.d(TAG, "observeBleConnect()")
        attemptConnectBleFlow.start()
            .observe(viewLifecycleOwner, Observer { progressPctWrapper ->
                when (progressPctWrapper) {
                    is ProgressUpdateEvent -> progressBarBluetoothConnecting.progress =
                        progressPctWrapper.progressPct
                    is TimeoutEvent -> {
//                        convertProgressToErrorOnFailure()
                        showErrorThenRetry()
                        ZepLog.e(TAG, "startBluetoothLeDiscovery() observable: timeout!")
                    }
                }
            })
    }

    private fun navigateToReconnectFrag() {
        NavHostFragment.findNavController(this@ConnectViaBleFrag)
            .navigate(ConnectViaBleFragDirections.actionNavConnectViaBleToNavCheckIsPaired())
    }

    private fun showErrorCore(
        titleResId: Int
    ) {
        textViewLabelTitle?.text = resources.getString(titleResId)
        textViewLabelStatus?.text = resources.getString(R.string.status_hearable_connect_fail)
        progressBarBluetoothConnecting?.visibility = View.INVISIBLE
        imageViewError?.visibility = View.VISIBLE
        lottiePairing?.cancelAnimation()
        viewModel.doActionAfterDelay(1000) {
            startFadeOutFor(progressConnect)
            startFadeInFor(buttonNextConnection)
            buttonNextConnection?.visibility = View.VISIBLE
            textViewLabelStatus?.visibility = View.INVISIBLE
            imageViewError?.visibility = View.INVISIBLE
        }
    }

    private fun showErrorThenRetryCore(
        titleResId: Int,
        actionTextResId: Int,
        action: () -> Unit
    ) {
        showErrorCore(titleResId)
        viewModel.doActionAfterDelay(1000) {
            buttonNextConnection?.text = resources.getString(actionTextResId)
            buttonNextConnection?.setOnClickListener {
                action()
            }
        }
    }

    /**
     * Briefly alter layout to show "success" indication:
     *  - change title
     *  - fade out progress-and-next-button combination widget
     * then invoke provided action e.g. navigate away to next screen...
     */
    private fun showSuccess(action: () -> Unit) {
        textViewLabelTitle?.text = resources.getString(R.string.title_ble_connect_success)
        startFadeOutFor(progressConnect)
        viewModel.doActionAfterDelay(1000) {
            action()
        }
    }

    private fun showErrorThenRetry() {
        showErrorThenRetryCore(
            R.string.label_title_please_try_again,
            R.string.button_error_try_again
        ) { navigateToReconnectFrag() }
    }

    /**
     * For returning user, the moment BLE connected go to user verification screen;
     * on error show dialog and take them back to start screen.
     */
    private fun loginFlowOnBleConnect() {
        viewModel.hearableDatasetFromDBForHearableID.observe(
            viewLifecycleOwner,
            Observer { respObjSgl ->
                respObjSgl.getContentIfNotHandled()?.let { hearableAssocOneToOne ->
                    if (hearableAssocOneToOne.hearableId.isNotEmpty() && hearableAssocOneToOne.wearerId.isNotEmpty()) {
                        viewModel.wearerId = hearableAssocOneToOne.wearerId
                        NavHostFragment.findNavController(this@ConnectViaBleFrag)
                            .navigate(ConnectViaBleFragDirections.actionNavConnectViaBleToNavInsertEarpieceVerifyUser())
                    } else {
                        NavHostFragment.findNavController(this@ConnectViaBleFrag)
                            .navigate(ConnectViaBleFragDirections.actionNavConnectViaBleToNavDialogHearableDisconnected())
                    }
                }
            })
    }

    /**
     * For new user, the moment we receive Wearer ID response from server go to registration tutorial screen;
     * on error show dialog and take them back to start screen.
     */
    private fun signupFlowOnWearerIdServerResponse(respObj: NecHearableWearerIdRespBase) {
        val toastMsg = when (respObj) {
            is NecHearableWearerIdSuccess -> {
                // Note: brand new wearerID required for every registration attempt; even if 90% completed,
                // on next app launch will need to start again with a different, brand new wearer ID from server
                viewModel.wearerId = respObj.wearerId
                NavHostFragment.findNavController(this@ConnectViaBleFrag)
                    .navigate(ConnectViaBleFragDirections.actionNavConnectViaBleToNavInsertEarpieceTutorial())
                resources.getString(R.string.toast_wearer_id_request_success)
            }
            is NecHearableWearerIdFail -> {
                NavHostFragment.findNavController(this@ConnectViaBleFrag)
                    .navigate(ConnectViaBleFragDirections.actionNavConnectViaBleToNavDialogHearableDisconnected())
                resources.getString(R.string.toast_wearer_id_request_error)
            }
        }
        AlertHelper.makeToast(context, toastMsg)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ZepLog.d(TAG, "onActivityCreated(): intro")

        attemptConnectBleFlow = viewModel.attemptConnectBluetoothLeFlow()

        val attemptInitMqttFlow = viewModel.attemptInitMqttFlow()
        textViewLabelStatus.text = resources.getString(R.string.status_init_sdk)
        attemptInitMqttFlow.start()
            .observe(viewLifecycleOwner, Observer { progressPctWrapper ->
                when (progressPctWrapper) {
                    is ProgressUpdateEvent -> {
                        progressBarBluetoothConnecting.progress =
                            progressPctWrapper.progressPct
                    }
                    is TimeoutEvent -> {
//                        convertProgressToErrorOnFailure()
                        showErrorThenRetry()
                        ZepLog.e(TAG, "attemptInitMqttWithTimeout.observable: timeout!")
                    }
                }
            })

        viewModel.mqttInitStatus.observe(viewLifecycleOwner, Observer { mqttStatusWrapper ->
            ZepLog.d(TAG, "onActivityCreated(): mqttInitStatus observer")
            val toastMsg = when (mqttStatusWrapper) {
                is NecHearableMqttSuccess -> {
                    attemptInitMqttFlow.isCancelled = true
                    ZepLog.d(TAG, "onActivityCreated::mqttInitStatus observer: $mqttStatusWrapper")
                    textViewLabelStatus.text =
                        resources.getString(R.string.label_progress_bluetooth_connecting)
                    observeBleConnect()
                    if (mqttStatusWrapper.isOK) resources.getString(R.string.toast_mqtt_init_success) else resources.getString(
                        R.string.toast_mqtt_init_error
                    )
                }
                is NecHearableMqttFail -> resources.getString(R.string.toast_mqtt_init_error)
                else -> ""
            }
            AlertHelper.makeToast(context, toastMsg)
        })

        viewModel.bluetoothLeConnected.observe(
            viewLifecycleOwner,
            Observer { necSdkRespObj ->
                attemptConnectBleFlow.isCancelled = true
                val toastMsg = when (necSdkRespObj) {
                    is NecHearableSimpleSuccess -> {
                        if (necSdkRespObj.isOK) {
                            showSuccess {
                                when (viewModel.loginSignup) {
                                    INavigationContract.LoginSignup.Login -> {
                                        loginFlowOnBleConnect()
                                    }
                                    INavigationContract.LoginSignup.Signup -> {
                                        viewModel.requestWearerId()
                                    }
                                }
                            }
                            resources.getString(R.string.toast_ble_connect_success)
                        } else {
                            showErrorThenRetry()
                            ZepLog.e(
                                TAG,
                                "onActivityCreated::bluetoothLeConnected observer: WARNING: success w/problem! necSdkRespObj: $necSdkRespObj"
                            )
                            resources.getString(R.string.toast_ble_connect_error)
                        }
                    }
                    else -> {
                        showErrorThenRetry()
                        ZepLog.e(
                            TAG,
                            "onActivityCreated::bluetoothLeConnected observer: ERROR: necSdkRespObj: $necSdkRespObj"
                        )
                        resources.getString(R.string.toast_ble_connect_error)
                    }
                }
                AlertHelper.makeToast(context, toastMsg)
            })

        // Only triggered in response to request to server for new WearerID
        viewModel.necWearerIdResponse.observe(viewLifecycleOwner, Observer { respObjSgl ->
            respObjSgl.getContentIfNotHandled()?.let { respObj ->
                signupFlowOnWearerIdServerResponse(respObj)
            }
        })

        viewModel.necHearableSdkError.observe(viewLifecycleOwner, Observer { btMsgObj ->
            attemptConnectBleFlow.isCancelled = true
            ZepLog.e(TAG, "necHearableSdkError::NecHearableSdkError observer: intro: $btMsgObj")
            when (btMsgObj) {
                is NecHearableSdkError -> {
                    ZepLog.e(
                        TAG,
                        "necHearableSdkError::NecHearableSdkError observer: ${btMsgObj.msg}"
                    )
                    showErrorThenRetry()
                }
                else -> ZepLog.e(
                    TAG,
                    "necHearableSdkError::NecHearableSdkError observer: NOTICE: unexpected type: $btMsgObj"
                )
            }
        })
    }
}
