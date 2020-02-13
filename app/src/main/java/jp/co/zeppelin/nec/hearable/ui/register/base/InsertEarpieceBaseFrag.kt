package jp.co.zeppelin.nec.hearable.ui.register.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_insert_earpiece.*

/**
 * Base fragment for displaying "insert earpiece" animations
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class InsertEarpieceBaseFrag : BaseLocationPermissionFrag() {
    private val TAG = InsertEarpieceBaseFrag::class.java.simpleName

    enum class ScreenType {
        Invalid,
        Tutorial,
        AdditionalFeaturesRequired,
        VerifyUser
    }

    // This core screen is used twice; first for the tutorial (no dots), then after tutorial
    // completed (this time with dots)
    protected var screenType = ScreenType.Invalid

    private fun initScreen() {
        when (screenType) {
            ScreenType.AdditionalFeaturesRequired -> {
                textViewTitleStateB.text =
                    resources.getString(R.string.insert_earpiece_verify_user_title_2of2)
            }
            ScreenType.VerifyUser -> {
                textViewTitleStateA.text =
                    resources.getString(R.string.insert_earpiece_verify_user_title_1of2)
                textViewTitleStateB.text =
                    resources.getString(R.string.insert_earpiece_verify_user_title_2of2)
                textViewDescr.text = resources.getString(R.string.insert_earpiece_verify_user_descr)
                buttonNextStateB.text =
                    resources.getString(R.string.insert_earpiece_verify_user_button_next)
            }
            else -> {
            }
        }
        val lottieFadeInMillisec =
            resources.getInteger(R.integer.lottie_anim_fade_in_hack_millisec).toLong()
        startFadeInFor(lottieInsertEarpiece1of2, lottieFadeInMillisec)
        lottieInsertEarpiece1of2.addAnimatorUpdateListener { animation ->
            // Empirically, animation.animatedValue never reaches 1.0
            if (animation.animatedFraction >= 1.0) {
                lottieInsertEarpiece1of2.removeAllUpdateListeners()
            }
        }
    }

    private fun crossFadeToNextScreen() {
        val fastFadeMillisec = resources.getInteger(R.integer.anim_fast_fade_millisec).toLong()
        startFadeOutForLottieAnim(lottieInsertEarpiece1of2)
        startFadeInFor(lottieInsertEarpiece2of2, fastFadeMillisec)
        lottieInsertEarpiece2of2.playAnimation()
        val animFadeOut: Animation = AnimationUtils.loadAnimation(context, R.anim.fadeout)
        textViewTitleStateA.startAnimation(animFadeOut)
        textViewDescr.startAnimation(animFadeOut)
        buttonNextStateA.startAnimation(animFadeOut)

        startFadeInFor(textViewTitleStateB)
        buttonNextStateB.visibility = View.VISIBLE
        startFadeInFor(buttonNextStateB)
    }

    protected fun updateScreenOnStateChange() {
        lottieInsertEarpiece1of2.resumeAnimation()
        crossFadeToNextScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_insert_earpiece, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initScreen()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonNextStateA.setOnClickListener {
            // Simply advance to next state within this "screen"
            updateScreenOnStateChange()
        }
    }
}
