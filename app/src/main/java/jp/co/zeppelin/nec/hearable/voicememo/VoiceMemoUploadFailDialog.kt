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
 * Dialog displayed when recorded voicememo server upload fails
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class VoiceMemoUploadFailDialog : DialogFragment() {
    private val TAG = VoiceMemoUploadFailDialog::class.java.simpleName

    private lateinit var viewModel: ViewModelCommon

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        ZepLog.d(TAG, "VoiceMemoUploadFailDialog.onCreateDialog(): intro, act $activity")
        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        }
            ?: throw Exception("VoiceMemoUploadFailDialog::onCreateDialog(): ERROR: Invalid Activity")

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.voicememo_dialog_upload_fail_title)
                .setMessage(R.string.voicememo_dialog_upload_fail_descr)
                .setPositiveButton(
                    R.string.button_ok,
                    DialogInterface.OnClickListener { _, _ ->
                    })
            // Create the AlertDialog object and return it
            builder.create()
        }
            ?: throw IllegalStateException("VoiceMemoUploadFailDialog.onCreateDialog(): Activity cannot be null")
    }
}
