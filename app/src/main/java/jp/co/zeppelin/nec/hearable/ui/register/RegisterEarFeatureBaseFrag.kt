package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import androidx.lifecycle.Observer
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.NecHearableSdkError
import jp.co.zeppelin.nec.hearable.domain.model.TimeoutEvent
import jp.co.zeppelin.nec.hearable.helper.AlertHelper
import jp.co.zeppelin.nec.hearable.necsdkwrapper.coro.ColdFlowWTimeout
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.*

/**
 * Play set of multiple registration sounds (at least 5 required for user registration)
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class RegisterEarFeatureBaseFrag : PlaySoundBaseFrag() {
    private val TAG = RegisterEarFeatureBaseFrag::class.java.simpleName

    protected var isRegistrationComplete = false

    var timeoutFlow: ColdFlowWTimeout? = null
    override var screenSpecificActionStatePlaying = {
        timeoutFlow = viewModel.registerEarTimeoutFlow()
        timeoutFlow?.let { timeoutFlowChecked ->
            timeoutFlowChecked.start()
                .observe(viewLifecycleOwner, Observer { progressPctWrapper ->
                    when (progressPctWrapper) {
                        is TimeoutEvent -> {
                            // Probable "hard" failure (nothing to upload) so skip straight to "done"
                            // i.e. advance two states
                            advancePlaybackState()
                            advancePlaybackState()
                            ZepLog.e(TAG, "setStatePlaying(): observable: timeout!")
                        }
                        else -> {
                        }
                    }
                })
        }
        Unit
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // On finish measure ear features via local playback of sound
        viewModel.necHearableEarFeatureMeasureResponse.observe(
            viewLifecycleOwner,
            Observer { respObjSgl ->
                respObjSgl.getContentIfNotHandled()?.let { respObj ->
                    timeoutFlow?.isCancelled = true
                    advancePlaybackState()
                    val toastMsg = when (respObj) {
                        is NecHearableMeasureEarFeatSuccess -> {
                            viewModel.attemptSendEarFeatureToServer()
                            ZepLog.d(TAG, "necHearableStartFeatureResponse::observer: $respObj")
                            resources.getString(R.string.toast_register_detected_ear_feature_success)
                        }
                        is NecHearableMeasureEarFeatFail -> resources.getString(R.string.toast_register_detected_ear_feature_error)
                    }
                    AlertHelper.makeToast(context, toastMsg)
                }
            })

        // On success send to server result of ear feature measurement
        viewModel.necHearableEarFeatureServerSendResponse.observe(
            viewLifecycleOwner,
            Observer { featSendRespObjSgl ->
                featSendRespObjSgl.getContentIfNotHandled()?.let { featSendRespObj ->
                    advancePlaybackState()
                    val toastMsg = when (featSendRespObj) {
                        is NecHearableFeatSuccessRegisterComplete -> {
                            isRegistrationComplete = true
                            ZepLog.d(TAG, "necHearableResponse::observer: ear register complete!")
                            resources.getString(R.string.toast_send_final_detected_ear_feature_success)
                        }
                        is NecHearableFeatSuccessRegisterIncomplete -> {
                            ZepLog.d(
                                TAG,
                                "necHearableResponse::observer: OK, need ${featSendRespObj.deficientNum} more!"
                            )
                            resources.getString(
                                R.string.format_toast_send_detected_ear_feature_success,
                                featSendRespObj.deficientNum
                            )
                        }
                        is NecHearableFeatSendError -> {
                            ZepLog.e(
                                TAG,
                                "necHearableResponse::observer: WARNING: ${featSendRespObj.message}"
                            )
                            resources.getString(R.string.toast_send_detected_ear_feature_error)
                        }
                        is NecHearableFeatSendFail -> {
                            ZepLog.e(
                                TAG,
                                "necHearableResponse::observer: ERROR: ${featSendRespObj.message}"
                            )
                            resources.getString(R.string.toast_send_detected_ear_feature_error)
                        }
                    }
                    AlertHelper.makeToast(context, toastMsg)
                }
            })

        viewModel.necHearableSdkError.observe(viewLifecycleOwner, Observer { respObj ->
            when (respObj) {
                is NecHearableSdkError -> ZepLog.e(
                    TAG,
                    "necHearableSdkError::observer: ERROR: ${respObj.msg}"
                )
                else -> ZepLog.e(
                    TAG,
                    "necHearableSdkError::observer: NOTICE: unexpected response type ${respObj.javaClass.simpleName}: $respObj"
                )
            }
        })
    }
}
