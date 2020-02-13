package jp.co.zeppelin.nec.hearable.helper

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import jp.co.zeppelin.nec.hearable.R

/**
 * Useful UI animation utilities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
object AnimHelper {

    /**
     * Slowly pulse UI element e.g. highlight rectangle to focus user attention
     *
     * NOTE: anecdotally the max alpha this will use is whatever is specified in layout xml, i.e.
     * just leave it at 1.0 (e.g. if "android:alpha" in layout xml set to 0.1 will on fade in to
     * barely visible; if set to 0.0 will never appear at all)
     *
     * @param context  context as usual
     * @param viewToPulse  UI widget to pulse
     */
    fun pulseAnimFor(context: Context?, viewToPulse: View) {
        val animPulse: Animation =
            AnimationUtils.loadAnimation(context, R.anim.pulse_alpha_slow)
        viewToPulse.startAnimation(animPulse)
    }

    /**
     * Custom pulse where "dark" state will be held for some duration
     *
     * @param context  context as usual
     * @param viewToPulse  UI widget to pulse
     * @param pulseAlphaResId  animation resource ID for "pulse" phase
     * @param holdAlphaOffResId animation resource ID for "dark" phase
     */
    fun startPulseAlpha(
        context: Context?,
        viewToPulse: View,
        pulseAlphaResId: Int,
        holdAlphaOffResId: Int
    ) {
        val animPulseAlpha: Animation =
            AnimationUtils.loadAnimation(context, pulseAlphaResId)
        animPulseAlpha.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                startDarkAnim(context, viewToPulse, pulseAlphaResId, holdAlphaOffResId)
            }

            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        viewToPulse.startAnimation(animPulseAlpha)
    }

    /**
     * "dark" state coupled with startPulseAlpha()
     *
     * @param context  context as usual
     * @param viewToPulse  UI widget to pulse
     * @param pulseAlphaResId  animation resource ID for "pulse" phase
     * @param holdAlphaOffResId animation resource ID for "dark" phase
     */
    private fun startDarkAnim(
        context: Context?,
        viewToPulse: View,
        pulseAlphaResId: Int,
        holdAlphaOffResId: Int
    ) {
        val animDarkAlpha: Animation =
            AnimationUtils.loadAnimation(context, holdAlphaOffResId)
        animDarkAlpha.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                startPulseAlpha(context, viewToPulse, pulseAlphaResId, holdAlphaOffResId)
            }

            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        viewToPulse.startAnimation(animDarkAlpha)
    }

}
