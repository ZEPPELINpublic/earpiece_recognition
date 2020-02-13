package jp.co.zeppelin.nec.hearable.ui.register

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import kotlinx.android.synthetic.main.frag_play_sounds.*

/**
 * Display error when authentication via ear biometric failed
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class AdditionalEarFeatureRegisterAttemptsFrag : RegisterEarFeatureBaseFrag() {
    private val TAG = AdditionalEarFeatureRegisterAttemptsFrag::class.java.simpleName

    override var screenSpecificActionSetup = {
        dotsIndicator.visibility = View.INVISIBLE
    }

    override var screenSpecificActionStateReady = {
        lottiePlaySound.setAnimation(R.raw.lottie_04_ripple_big)
        lottieSendFeatureToServer.setAnimation(R.raw.lottie_05_trace_loop_big)
        lottieFeatureSentToServer.setAnimation(R.raw.lottie_06_trace_done_big)
        textViewLabelTitle.text =
            resources.getString(R.string.label_registration_play_sound_title)
    }

    override var screenSpecificActionStatePlaying = {
        super.screenSpecificActionStatePlaying()
        viewModel.attemptMeasureEarFeature()
        textViewLabelTitle.text =
            resources.getString(R.string.register_ear_next_4_title_playback)
    }

    /**
     * Show title bar, button in purple the second time through (if first attempt unsuccessful)
     */
    private fun setAdditionalEarFeatureAcquisitionTheme(context: Context) {
        colorThemeResId = R.color.play_sound_alt_theme_purple
        textViewTopBar.setBackgroundColor(ContextCompat.getColor(context, colorThemeResId))
        frameLayoutLoadingIndicator.setBackgroundResource(R.drawable.wait_button_purple)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.AdditionalEarFeatureAcquisition
        context?.apply {
            setAdditionalEarFeatureAcquisitionTheme(this)
//            LottieAnimReg02(this, lottiePlaySound, colorThemeResId)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        frameLayoutLoadingIndicator.setOnClickListener {
            if (!isBusy) {
                if (isRegistrationComplete) {
                    NavHostFragment.findNavController(this@AdditionalEarFeatureRegisterAttemptsFrag)
                        .navigate(AdditionalEarFeatureRegisterAttemptsFragDirections.actionNavAdditionalEarFeatureRegisterAttemptsToNavRegistrationComplete())
                } else {
                    advancePlaybackState()
                }
            }
        }
    }
}
