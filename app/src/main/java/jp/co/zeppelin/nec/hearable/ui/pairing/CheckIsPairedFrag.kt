package jp.co.zeppelin.nec.hearable.ui.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_bluetooth_reconnect.*

@kotlinx.coroutines.ExperimentalCoroutinesApi
@kotlinx.coroutines.InternalCoroutinesApi
class CheckIsPairedFrag : BaseLocationPermissionFrag() {
    private val TAG = CheckIsPairedFrag::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_bluetooth_reconnect, container, false)

    private fun startPulseAlpha() {
        val animPulseAlpha: Animation =
            AnimationUtils.loadAnimation(context, R.anim.pulse_alpha_fast)
        animPulseAlpha.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                startDarkAnim()
            }

            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        imageViewLedGlow.startAnimation(animPulseAlpha)
    }

    private fun startDarkAnim() {
        val animDarkAlpha: Animation =
            AnimationUtils.loadAnimation(context, R.anim.hold_alpha_off)
        animDarkAlpha.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                startPulseAlpha()
            }

            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        imageViewLedGlow.startAnimation(animDarkAlpha)
    }

    private fun startImageAnim() {
        startPulseAlpha()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageViewLedGlowGreen.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonNext.setOnClickListener {
            NavHostFragment.findNavController(this@CheckIsPairedFrag)
                .navigate(CheckIsPairedFragDirections.actionNavCheckIsPairedToNavConnectViaBle())
        }
        startImageAnim()
    }
}
