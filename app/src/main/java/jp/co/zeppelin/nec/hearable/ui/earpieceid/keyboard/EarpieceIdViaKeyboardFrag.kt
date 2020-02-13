package jp.co.zeppelin.nec.hearable.ui.earpieceid.keyboard

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId.hasUserEnteredPossiblyValidDeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import kotlinx.android.synthetic.main.frag_earpiece_id_via_keyboard.*

/**
 * Screen for entering Hearable ID via keyboard
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class EarpieceIdViaKeyboardFrag : BaseVmFrag() {
    private val TAG = EarpieceIdViaKeyboardFrag::class.java.simpleName

    var isZoomAnimFinished = false
    var isTextValid = false

    fun enableNextButton() {
        buttonNext.isEnabled = isZoomAnimFinished && isTextValid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_earpiece_id_via_keyboard, container, false)

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSeq: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(charSeq: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            val charSeq = textInputEditText.text
            ZepLog.d(TAG, "onTextChanged(): ${charSeq?.length} chars")
            charSeq?.let { charSeqChecked ->
                val deviceIdViaKeyboardResult =
                    hasUserEnteredPossiblyValidDeviceId(charSeqChecked.toString())
                if (deviceIdViaKeyboardResult.isValid) {
                    textInputLayout.editText?.removeTextChangedListener(this)
                    textInputEditText.setText(deviceIdViaKeyboardResult.validId)
                    textInputLayout.editText?.addTextChangedListener(this)
                    viewModel.updateTargetDeviceId(charSeqChecked.toString())
                    isTextValid = true
                } else {
                    isTextValid = false
                }
            }
            enableNextButton()
        }
    }

    private fun setupEditText() {
        textInputLayout.editText?.addTextChangedListener(textWatcher)
    }

    private fun startImageAnim() {
        val animScaleUp: Animation =
            AnimationUtils.loadAnimation(context, R.anim.earpiece_label_scale)
        // Note: need to set "fillAfter" programatically, no effect in xml
        animScaleUp.fillAfter = true
        imageViewEarpieceLabel.startAnimation(animScaleUp)
        animScaleUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                isZoomAnimFinished = true
                enableNextButton()
            }

            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        val animScaleNPulse: Animation =
            AnimationUtils.loadAnimation(context, R.anim.earpiece_label_scale_n_pulse)
        animScaleNPulse.fillAfter = true
        imageViewEarpieceLabelHighlightRect
            .startAnimation(animScaleNPulse)
    }

    private fun hideSoftKeyboard() {
        context?.let { contextNN ->
            view?.let { viewNN ->
                if (viewNN.requestFocus()) {
                    val imm =
                        contextNN.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewNN.windowToken, 0)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEditText()
        startImageAnim()

        imageViewButtonCamera.setOnClickListener {
            hideSoftKeyboard()
            NavHostFragment.findNavController(this@EarpieceIdViaKeyboardFrag)
                .navigate(EarpieceIdViaKeyboardFragDirections.actionNavEarpieceIdViaKeyboardToNavQrCodePrep())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textInputLayout.editText?.setText(viewModel.lastTargetDeviceId())

        buttonNext.setOnClickListener {
            textInputLayout.editText?.text?.toString()?.let { _ ->
                NavHostFragment.findNavController(this@EarpieceIdViaKeyboardFrag)
                    .navigate(EarpieceIdViaKeyboardFragDirections.actionNavEarpieceIdViaKeyboardToNavConfirmHearableId())
            }
        }
    }
}
