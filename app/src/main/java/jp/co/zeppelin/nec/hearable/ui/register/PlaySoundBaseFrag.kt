package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_play_sounds.*

/**
 * Common base fragment for playing digital sound via NEC Hearable for registration/authentication
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class PlaySoundBaseFrag : BaseLocationPermissionFrag() {
    private val TAG = PlaySoundBaseFrag::class.java.simpleName

    protected var statusBarColorTheme: Int = 0

    enum class ScreenType {
        Invalid,
        Tutorial,
        NextFour,
        AdditionalEarFeatureAcquisition,
        VerifyUser
    }

    enum class PlaybackState {
        Ready,
        Playing,
        SendToServer,
        Done,
        PostSoundPlayback
    }

    protected var screenType = ScreenType.Invalid
    protected var playbackState = PlaybackState.Ready
    protected var dotPositionIdx = 0
    protected var dotCount = 0
    protected var topBarTextPlaybackResId = R.string.top_bar_sound_playing
    protected var colorThemeResId = R.color.play_sound_alt_theme_purple
    protected var isBusy = false

    protected open var screenSpecificActionSetup: () -> Unit = {}
    protected open var screenSpecificActionStateReady: () -> Unit = {}
    protected open var screenSpecificActionStatePlaying: () -> Unit = {}

    protected fun advancePlaybackState() {
        when (playbackState) {
            PlaybackState.Ready -> {
                setStatePlaying()
            }
            PlaybackState.Playing -> {
                when (screenType) {
                    ScreenType.VerifyUser -> setStateDone()
                    else -> setStateSendToServer()
                }
            }
            PlaybackState.SendToServer -> {
                setStateDone()
            }
            PlaybackState.Done -> {
                when (screenType) {
                    ScreenType.Tutorial,
                    ScreenType.VerifyUser -> setStateDone()
                    else -> setStatePostSoundPlayback()
                }
            }
            PlaybackState.PostSoundPlayback -> {
                setStateReady()
            }
        }
    }

    protected fun isFinished(): Boolean {
        return when (screenType) {
            ScreenType.NextFour -> dotPositionIdx >= dotCount
            ScreenType.Tutorial,
            ScreenType.VerifyUser -> playbackState == PlaybackState.Done
            else -> false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_play_sounds, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenSpecificActionSetup()
        setStateReadyCore()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            statusBarColorTheme = when (screenType) {
                ScreenType.AdditionalEarFeatureAcquisition -> ContextCompat.getColor(
                    this.applicationContext,
                    R.color.play_sound_alt_theme_purple
                )
                else -> ContextCompat.getColor(this.applicationContext, R.color.nec_blue)
            }
            this.window?.statusBarColor = statusBarColorTheme
        }
    }

    private fun stopLottieAnim() {
        lottiePlaySound.cancelAnimation()
        lottiePlaySound.progress = 0f
    }

    private fun fadeLottiePlayToSend() {
        startFadeOutForLottieAnim(lottiePlaySound)
        startFadeInForLottieAnim(lottieSendFeatureToServer)
        lottieSendFeatureToServer.playAnimation()
    }

    private fun fadeLottieSendToSent() {
        startFadeOutForLottieAnim(lottieSendFeatureToServer)
        startFadeInForLottieAnim(lottieFeatureSentToServer)
        lottieFeatureSentToServer.repeatCount = 1
        lottieFeatureSentToServer.resumeAnimation()
    }

    private fun fadeLottieSentToPlay() {
        startFadeOutForLottieAnim(lottieFeatureSentToServer)
        startFadeInForLottieAnim(lottiePlaySound)
    }

    private fun setStateReadyCore() {
        activity?.apply {
            this.window?.statusBarColor = statusBarColorTheme
        }
        isBusy = false
        screenSpecificActionStateReady()
        fadeLottieSentToPlay()
        textViewTopBar.text = resources.getString(R.string.top_bar_play_sound)
        textViewButtonNext.visibility = View.VISIBLE
        textViewButtonNext.text = resources.getString(R.string.button_next_registration_play_sound)
        loadingIndicator.visibility = View.INVISIBLE
        playbackState = PlaybackState.Ready
    }

    private fun setStateReady() {
        if (!isFinished()) {
            setStateReadyCore()
            stopLottieAnim()
        } else {
            setStateDone()
        }
    }

    private fun setStatePlaying() {
        if (!isFinished()) {
            isBusy = true
            screenSpecificActionStatePlaying()
            lottiePlaySound.playAnimation()
            textViewTopBar.text = resources.getString(topBarTextPlaybackResId)
            textViewButtonNext.visibility = View.INVISIBLE
            loadingIndicator.visibility = View.VISIBLE
            playbackState = PlaybackState.Playing
        }
    }

    private fun setStateSendToServer() {
        when (screenType) {
            ScreenType.Tutorial,
            ScreenType.NextFour,
            ScreenType.AdditionalEarFeatureAcquisition -> {
                fadeLottiePlayToSend()
            }
            else -> {
                lottiePlaySound.playAnimation()
            }
        }
        isBusy = true
        textViewTopBar.text =
            resources.getString(R.string.top_bar_sending)
        textViewLabelTitle.text =
            resources.getString(R.string.title_sending)
        playbackState = PlaybackState.SendToServer
    }

    protected fun advanceDotSelection() {
        when (dotPositionIdx) {
            0 -> dotsIndicator.setBackgroundResource(R.drawable.four_dots_1_ready)
            1 -> dotsIndicator.setBackgroundResource(R.drawable.four_dots_2_ready)
            2 -> dotsIndicator.setBackgroundResource(R.drawable.four_dots_3_ready)
            3 -> dotsIndicator.setBackgroundResource(R.drawable.four_dots_4_ready)
            4 -> dotsIndicator.setBackgroundResource(R.drawable.four_dots_4_done)
        }
    }

    private fun setStateDone() {
        ++dotPositionIdx
        if (screenType == ScreenType.NextFour) {
            advanceDotSelection()
        }
        when (screenType) {
            ScreenType.NextFour,
            ScreenType.AdditionalEarFeatureAcquisition,
            ScreenType.Tutorial -> {
                fadeLottieSendToSent()
            }
            else -> {
                lottiePlaySound.repeatCount = 1
                lottiePlaySound.resumeAnimation()
            }
        }
        isBusy = false
        textViewTopBar.text = resources.getString(R.string.top_bar_sound_playback_complete)
        textViewLabelTitle.text = resources.getString(R.string.label_sound_playback_complete_title)
        textViewButtonNext.visibility = View.VISIBLE
        textViewButtonNext.text = resources.getString(R.string.button_sound_playback_complete)
        loadingIndicator.visibility = View.INVISIBLE
        playbackState = PlaybackState.Done
    }

    private fun setStatePostSoundPlayback() {
        isBusy = false
        stopLottieAnim()
        when (screenType) {
            ScreenType.NextFour -> {
                // Reset system status bar color for unthemed "insert earpiece" screen
                activity?.apply {
                    this.window?.statusBarColor = 0
                }
                startFadeOutForLottieAnim(lottieFeatureSentToServer)
                startFadeInForLottieAnim(lottieInsertEarpiece2of2)
                lottieInsertEarpiece2of2.progress = 0f
                startFadeOutFastFor(textViewTopBar)
                textViewLabelTitle.text =
                    resources.getString(R.string.insert_earpiece_verify_user_title_2of2)
                textViewButtonNext.text =
                    resources.getString(R.string.insert_earpiece_tutorial_button_next)
            }
            ScreenType.AdditionalEarFeatureAcquisition -> {
                textViewLabelTitle.text =
                    resources.getString(R.string.register_ear_additional_incomplete_title)
                textViewButtonNext.text = resources.getString(R.string.next)
            }
            else -> {
            }
        }
        playbackState = PlaybackState.PostSoundPlayback
    }
}
