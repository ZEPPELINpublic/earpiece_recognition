package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import kotlinx.android.synthetic.main.frag_play_sounds.*

/**
 * Play first 5 digital sounds during user registration flow
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class PlayRegisterSoundsNextFourFrag : RegisterEarFeatureBaseFrag() {
    private val TAG = PlayRegisterSoundsNextFourFrag::class.java.simpleName

    override var screenSpecificActionSetup = {
        dotCount = resources.getInteger(R.integer.count_register_play_sound_remaining_times)
        dotsIndicator.setBackgroundResource(R.drawable.four_dots_1_ready)
    }

    override var screenSpecificActionStateReady = {
        lottiePlaySound.setAnimation(R.raw.lottie_01b_ripple_small)
        lottieSendFeatureToServer.setAnimation(R.raw.lottie_02_trace_loop_small)
        lottieFeatureSentToServer.setAnimation(R.raw.lottie_03_trace_done_small)
        startFadeInFastFor(textViewTopBar)
        startFadeOutForLottieAnim(lottieInsertEarpiece2of2)
        textViewLabelTitle.text =
            resources.getString(R.string.label_registration_play_sound_title)
    }

    override var screenSpecificActionStatePlaying = {
        super.screenSpecificActionStatePlaying()
        viewModel.attemptMeasureEarFeature()
        textViewLabelTitle.text =
            resources.getString(R.string.register_ear_next_4_title_playback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.NextFour
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        frameLayoutLoadingIndicator.setOnClickListener {
            if (!isBusy) {
                advancePlaybackState()
                if (isFinished()) {
                    if (isRegistrationComplete) {
                        NavHostFragment.findNavController(this@PlayRegisterSoundsNextFourFrag)
                            .navigate(PlayRegisterSoundsNextFourFragDirections.actionNavPlayRegisterSoundsNextFourToNavRegistrationComplete())
                    } else {
                        NavHostFragment.findNavController(this@PlayRegisterSoundsNextFourFrag)
                            .navigate(PlayRegisterSoundsNextFourFragDirections.actionNavPlayRegisterSoundsNextFourToNavAdditionalRegistrationRunsRequired())
                    }
                }
            }
        }
    }
}
