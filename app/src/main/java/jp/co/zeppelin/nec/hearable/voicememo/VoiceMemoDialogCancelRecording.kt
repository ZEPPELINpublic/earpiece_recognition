package jp.co.zeppelin.nec.hearable.voicememo

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommon
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommonFactory


/**
 * Dialog displayed when voicememo recording cancelled by user
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class VoiceMemoDialogCancelRecording : DialogFragment() {
    private val TAG = VoiceMemoDialogCancelRecording::class.java.simpleName

    private lateinit var viewModel: ViewModelCommon

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        ZepLog.d(TAG, "VoiceMemoDialogCancelRecording.onCreateDialog(): intro, act $activity")
        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        }
            ?: throw Exception("VoiceMemoDialogCancelRecording::onCreateDialog(): ERROR: Invalid Activity")

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.voicememo_dialog_cancel_title)
                .setMessage(R.string.voicememo_dialog_cancel_descr)
                .setPositiveButton(
                    R.string.button_ok,
                    DialogInterface.OnClickListener { _, _ ->
                        viewModel.cancelRecording()
                    })
                .setNegativeButton(
                    R.string.button_cancel,
                    DialogInterface.OnClickListener { _, _ ->
                    })
            // Create the AlertDialog object and return it
            builder.create()
        }
            ?: throw IllegalStateException("VoiceMemoDialogCancelRecording.onCreateDialog(): Activity cannot be null")
    }
}
