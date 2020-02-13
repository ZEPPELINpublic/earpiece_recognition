package jp.co.zeppelin.nec.hearable.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommon
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommonFactory

/**
 * Dialog displayed when hearable is not both paired (bonded) and connected via Bluetooth "Classic"
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class HearableNotBothBTClassicPairedNConnectedDialog : DialogFragment() {
    private lateinit var viewModel: ViewModelCommon

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        }
            ?: throw Exception("HearableNotBothBTClassicPairedNConnectedDialog::onCreateDialog(): ERROR: Invalid Activity")

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.dialog_bluetooth_pairing_failure_title)
                .setMessage(R.string.dialog_bluetooth_pairing_failure_descr)
                .setPositiveButton(
                    R.string.dialog_bluetooth_pairing_failure_button_need_help,
                    DialogInterface.OnClickListener { _, _ ->
                        NavHostFragment.findNavController(this@HearableNotBothBTClassicPairedNConnectedDialog)
                            .navigate(
                                HearableNotBothBTClassicPairedNConnectedDialogDirections.actionNavHearableNotBothBtClassicPairedNConnectedToNavHearablePowerPairConnectTutorial()
                            )
                    })
            // Create the AlertDialog object and return it
            builder.create()
        }
            ?: throw IllegalStateException("HearableNotBothBTClassicPairedNConnectedDialog::onCreateDialog(): Activity cannot be null")
    }
}
