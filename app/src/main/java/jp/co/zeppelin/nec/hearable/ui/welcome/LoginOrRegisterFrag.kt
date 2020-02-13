package jp.co.zeppelin.nec.hearable.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.airbnb.lottie.LottieDrawable
import jp.co.zeppelin.nec.hearable.BuildConfig
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.helper.AnimHelper
import jp.co.zeppelin.nec.hearable.navigation.INavigationContract
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import kotlinx.android.synthetic.main.frag_login_or_register.*

/**
 * When building screens around lottie animations it may be necessary to combine multiple
 * "screens" inside a single fragment to avoid transition glitches (lottie animation
 * assets take finite time to load and will "pop" after other elements are already visible)
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class LoginOrRegisterFrag : BaseVmFrag() {
    private val TAG = LoginOrRegisterFrag::class.java.simpleName

    private var didFadeInLottieAnimDeps = false
    // Reset start of lottie animation to loop blinking LED forever
    private val LOTTIE_LOOP_BLINKING_LED_START_FRACT = 0.37f

    private fun initScreen() {
        val lottieFadeInMillisec =
            resources.getInteger(R.integer.lottie_anim_fade_in_hack_millisec).toLong()
        startFadeInFor(lottieWelcomeEarpiece, lottieFadeInMillisec)
    }

    /**
     * After inital non-looping full animation, loop the final "blinking LED" portion forever
     */
    private fun beginLoopBlinkingLED() {
        lottieWelcomeEarpiece.removeAllUpdateListeners()
//        ZepLog.e(TAG, "lottieWelcomeEarpiece::animatorUpdateListener: removed listeners...")
        lottieWelcomeEarpiece.setMinProgress(LOTTIE_LOOP_BLINKING_LED_START_FRACT)
        // loop() deprecated but the damn thing just works
        lottieWelcomeEarpiece.repeatCount = LottieDrawable.INFINITE
        lottieWelcomeEarpiece.playAnimation()
    }

    private fun crossFadeToNextScreen() {
        // To reduce perceived connection time, init MQTT setup as near as possible to app launch
        // TODO: Placing this here is a hack to inject an implicit delay; attempting to do this immediately
        // TODO  (i.e. in onActivityCreated()) is expected to fail due to NEC service not yet ready
        viewModel.setMqttParameter()

        lottieWelcomeEarpiece.resumeAnimation()
        lottieWelcomeEarpiece.addAnimatorUpdateListener { animation ->
            // Empirically, animation.animatedValue never reaches 1.0
            if (animation.animatedFraction >= 1.0) {
//                ZepLog.e(TAG, "lottieWelcomeEarpiece::animatorUpdateListener: fract ${animation.animatedFraction}, val: ${animation.animatedValue}")
                beginLoopBlinkingLED()
            } else if (animation.animatedValue as Float > 0.3) {
                fadeInAfterPlayLottieAnim()
            }
        }
        val animFadeOut: Animation = AnimationUtils.loadAnimation(context, R.anim.fadeout)
        textViewTitleSplash.startAnimation(animFadeOut)
        textViewDescrSplash.startAnimation(animFadeOut)
        textViewEdgeId.startAnimation(animFadeOut)
        buttonSignup.startAnimation(animFadeOut)
        buttonLogin.startAnimation(animFadeOut)

        when (viewModel.loginSignup) {
            INavigationContract.LoginSignup.Login -> {
                textViewLabelTitle.text =
                    resources.getString(R.string.title_welcome_screen_2_login_flow)
                textViewLabelDescr.text =
                    resources.getString(R.string.descr_welcome_screen_2_login_flow)
            }
            INavigationContract.LoginSignup.Signup -> {
                startFadeInFor(textViewLabelNextHint)
            }
        }
        startFadeInFor(textViewLabelTitle)
        startFadeInFor(textViewLabelDescr)
        buttonNext.visibility = View.VISIBLE
        startFadeInFor(buttonNext)
    }

    private fun fadeInAfterPlayLottieAnim() {
        if (!didFadeInLottieAnimDeps) {
            startFadeInFor(textViewLabelSwitchDetail)
            imageViewDetailCircle.visibility = View.VISIBLE
            AnimHelper.pulseAnimFor(context, imageViewDetailCircle)
            didFadeInLottieAnimDeps = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_login_or_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initScreen()

        buttonLogin.isEnabled = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.necHearableStarted.observe(viewLifecycleOwner, Observer { _ ->
            if (BuildConfig.DEBUG) {
                textViewEdgeId.text = resources.getString(
                    R.string.format_edge_id,
                    viewModel.edgeId
                )
                textViewEdgeId.visibility = View.VISIBLE
            }
        })

        // Only enable "Login" button for returning users, if at least one user previously completed registration
        viewModel.registeredHearableDatasetsFromDB.observe(
            viewLifecycleOwner,
            Observer { registeredHearables ->
                if (registeredHearables.isNotEmpty()) {
                    buttonLogin.isEnabled = true
                }
            })

        buttonSignup.setOnClickListener {
            viewModel.loginSignup = INavigationContract.LoginSignup.Signup
            crossFadeToNextScreen()
        }

        buttonLogin.setOnClickListener {
            viewModel.loginSignup = INavigationContract.LoginSignup.Login
            crossFadeToNextScreen()
        }

        buttonNext.setOnClickListener {
            NavHostFragment.findNavController(this@LoginOrRegisterFrag)
                .navigate(LoginOrRegisterFragDirections.actionNavLoginOrRegisterToNavBluetoothSystemPairingTutorial())
        }
    }
}
