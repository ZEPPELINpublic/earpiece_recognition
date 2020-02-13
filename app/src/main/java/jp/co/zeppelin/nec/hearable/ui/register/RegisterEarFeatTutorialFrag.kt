package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import kotlinx.android.synthetic.main.frag_play_sounds.*

/**
 * Walk user through first attempt at inner ear "feature" measurement via digital sound playback
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class RegisterEarFeatTutorialFrag : RegisterEarFeatureBaseFrag() {
    private val TAG = RegisterEarFeatTutorialFrag::class.java.simpleName

    override var screenSpecificActionSetup = {
        dotsIndicator.visibility = View.GONE
    }

    override var screenSpecificActionStateReady = {
        lottiePlaySound.setAnimation(R.raw.lottie_01b_ripple_small)
        lottieSendFeatureToServer.setAnimation(R.raw.lottie_02_trace_loop_small)
        lottieFeatureSentToServer.setAnimation(R.raw.lottie_03_trace_done_small)
        textViewLabelTitle.text = resources.getString(R.string.register_ear_tutorial_title_screen_1)
    }

    override var screenSpecificActionStatePlaying = {
        super.screenSpecificActionStatePlaying()
        viewModel.attemptMeasureEarFeature()
        textViewLabelTitle.text =
            resources.getString(R.string.register_ear_tutorial_title_screen_2)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        screenType = ScreenType.Tutorial
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        frameLayoutLoadingIndicator.setOnClickListener {
            if (!isBusy) {
                advancePlaybackState()
                if (playbackState == PlaybackState.Done) {
                    NavHostFragment.findNavController(this@RegisterEarFeatTutorialFrag)
                        .navigate(
                            RegisterEarFeatTutorialFragDirections.actionNavRegisterEarFeatTutorialToRegisterEarFeatTutorialCompleteFrag()
                        )
                }
            }
        }
    }
}
