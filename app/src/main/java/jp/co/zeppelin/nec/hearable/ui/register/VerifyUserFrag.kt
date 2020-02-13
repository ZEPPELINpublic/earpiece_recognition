package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.TimeoutEvent
import jp.co.zeppelin.nec.hearable.necsdkwrapper.coro.ColdFlowWTimeout
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.NEC_HEARABLE_RESULT_OK
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.NecHearableAuthSubscribeFail
import jp.co.zeppelin.nec.hearable.necsdkwrapper.model.NecHearableAuthSubscribeSuccess
import kotlinx.android.synthetic.main.frag_play_sounds.*

/**
 * Authenticate user via ear biometric
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class VerifyUserFrag : PlaySoundBaseFrag() {
    private val TAG = VerifyUserFrag::class.java.simpleName

    private lateinit var requestAuthFlow: ColdFlowWTimeout

    override var screenSpecificActionSetup = {
        dotsIndicator.visibility = View.GONE
        lottiePlaySound.setAnimation(R.raw.lottie_authentication_one_shot)
        topBarTextPlaybackResId = R.string.top_bar_verifying_user
    }

    override var screenSpecificActionStateReady = {}

    override var screenSpecificActionStatePlaying = {
        requestAuthFlow.start()
            .observe(viewLifecycleOwner, Observer { progressPctWrapper ->
                when (progressPctWrapper) {
                    is TimeoutEvent -> {
                        NavHostFragment.findNavController(this@VerifyUserFrag)
                            .navigate(VerifyUserFragDirections.actionNavVerifyUserToNavVerifyUserFail())
                        ZepLog.e(TAG, "setStatePlaying(): observable: timeout!")
                    }
                    else -> {
                    }
                }
            })
        textViewLabelTitle.text =
            resources.getString(R.string.register_ear_next_4_title_playback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.VerifyUser
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requestAuthFlow = viewModel.requestAuthFlow(viewModel.wearerId)

        // Automatially start playback
        advancePlaybackState()

        // On finish measure ear features via local playback of sound
        viewModel.necHearableAuthSubscribeResponse.observe(
            viewLifecycleOwner,
            Observer { respObjSgl ->
                respObjSgl.getContentIfNotHandled()?.let { respObj ->
                    requestAuthFlow.isCancelled = true
                    when (respObj) {
                        is NecHearableAuthSubscribeSuccess -> {
                            if (respObj.result == NEC_HEARABLE_RESULT_OK) {
                                viewModel.setSdkWearerId()
                                NavHostFragment.findNavController(this@VerifyUserFrag)
                                    .navigate(VerifyUserFragDirections.actionNavVerifyUserToNavPostRegistrationComplete())
                                ZepLog.d(
                                    TAG,
                                    "necHearableAuthResponse::observer: success: $respObj"
                                )
                            } else {
                                NavHostFragment.findNavController(this@VerifyUserFrag)
                                    .navigate(VerifyUserFragDirections.actionNavVerifyUserToNavVerifyUserFail())
                                ZepLog.e(TAG, "necHearableAuthResponse::observer: failed: $respObj")
                            }
                        }
                        is NecHearableAuthSubscribeFail -> {
                            NavHostFragment.findNavController(this@VerifyUserFrag)
                                .navigate(VerifyUserFragDirections.actionNavVerifyUserToNavVerifyUserFail())
                            ZepLog.e(
                                TAG,
                                "necHearableAuthResponse::observer: NOTICE: failed: $respObj"
                            )
                        }
                        else -> ZepLog.e(
                            TAG,
                            "necHearableAuthResponse::observer: NOTICE: unexpected type $respObj!"
                        )
                    }
                }
            })
    }
}
