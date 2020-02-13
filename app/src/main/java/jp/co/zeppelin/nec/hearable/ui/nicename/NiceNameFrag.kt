package jp.co.zeppelin.nec.hearable.ui.nicename

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import kotlinx.android.synthetic.main.frag_enter_nice_name.*

/**
 * Screen allowing user to enter "nice" username for ease of selecting from list items later
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class NiceNameFrag : BaseVmFrag() {
    private val TAG = NiceNameFrag::class.java.simpleName
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_enter_nice_name, container, false)

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSeq: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(charSeq: CharSequence?, start: Int, before: Int, count: Int) {
            ZepLog.d(TAG, "onTextChanged(): ${charSeq?.length} chars")
            charSeq?.let { charSeqChecked ->
                if (charSeqChecked.isNotEmpty()) {
                    val niceUserName = charSeqChecked.toString()
                    viewModel.checkNiceUsernameAlreadyInUse(niceUserName) { foundNiceUsername ->
                        if (foundNiceUsername) {
                            textViewErrorNameAlreadyInUse?.visibility = View.VISIBLE
                            buttonNext?.isEnabled = false
                        } else {
                            textViewErrorNameAlreadyInUse?.visibility = View.INVISIBLE
                            viewModel.niceUsername = niceUserName
                            buttonNext?.isEnabled = true
                        }
                    }
                } else {
                    buttonNext.isEnabled = false
                }
            }
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonNext.setOnClickListener {
            textInputLayout.editText?.text?.toString()?.let { _ ->
                NavHostFragment.findNavController(this@NiceNameFrag)
                    .navigate(NiceNameFragDirections.actionNavEnterNiceNameToNavEnterEarpieceId())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        textInputLayout.editText?.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        textInputLayout.editText?.removeTextChangedListener(textWatcher)
        super.onPause()
    }
}
