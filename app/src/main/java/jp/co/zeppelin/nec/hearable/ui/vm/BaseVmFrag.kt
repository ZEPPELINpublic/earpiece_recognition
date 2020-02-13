package jp.co.zeppelin.nec.hearable.ui.vm

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import jp.co.zeppelin.nec.hearable.R

/**
 * Hoist common code to shared base class; in this case all fragments share common AAC ViewModel
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class BaseVmFrag : Fragment() {
    private val TAG = BaseVmFrag::class.java.simpleName
    protected lateinit var viewModel: ViewModelCommon

    /**
     * Note: unable to get fade in working via xml animation file (fade out works perfectly)
     */
    protected fun startFadeInFor(
        view: View?,
        durationMilliSec: Long = resources.getInteger(R.integer.anim_fadeout_millisec).toLong()
    ): ObjectAnimator? {
        view?.let {
            val objectAnimator =
                ObjectAnimator.ofFloat(view, "alpha", view.alpha, 1f)
                    .setDuration(durationMilliSec)
            objectAnimator.start()
            return objectAnimator
        }
        return null
    }

    protected fun startFadeOutFor(
        view: View?,
        durationMilliSec: Long = resources.getInteger(R.integer.anim_fadeout_millisec).toLong()
    ): ObjectAnimator? {
        view?.let {
            val objectAnimator =
                ObjectAnimator.ofFloat(view, "alpha", view.alpha, 0f)
                    .setDuration(durationMilliSec)
            objectAnimator.start()
            return objectAnimator
        }
        return null
    }

    protected fun startFadeInFastFor(view: View?): ObjectAnimator? {
        return startFadeInFor(
            view,
            resources.getInteger(R.integer.anim_fast_fade_millisec).toLong()
        )
    }

    protected fun startFadeOutFastFor(view: View?): ObjectAnimator? {
        return startFadeOutFor(
            view,
            resources.getInteger(R.integer.anim_fast_fade_millisec).toLong()
        )
    }

    protected fun startFadeInForLottieAnim(lottieAnimWidget: View) {
        ObjectAnimator.ofFloat(lottieAnimWidget, "alpha", lottieAnimWidget.alpha, 1f)
            .setDuration(resources.getInteger(R.integer.lottie_anim_fade_fast_hack_millisec).toLong())
            .start()
    }

    /**
     * Fade out via xml animation resource doesn't stick; immediately pops back to fully opaque,
     * so go programmatic instead
     */
    protected fun startFadeOutForLottieAnim(lottieAnimWidget: View) {
        ObjectAnimator.ofFloat(lottieAnimWidget, "alpha", lottieAnimWidget.alpha, 0f)
            .setDuration(resources.getInteger(R.integer.lottie_anim_fade_fast_hack_millisec).toLong())
            .start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        } ?: throw Exception("onActivityCreated(): ERROR: Invalid Activity")
    }
}
