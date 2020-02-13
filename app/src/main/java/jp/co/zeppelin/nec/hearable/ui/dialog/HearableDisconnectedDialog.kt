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
 * Dialog which will appear when user is on home screen and connection to hearable lost
 *
 * Background/motivation
 * ---------------------
 * A non-trivial effort was made to dynamically deal with disconnection and reconnection of hearable
 * while on the home screen.  This is complicated because:
 *   - both bluetooth "classic" and BLE connections need to be established
 *   - for this reason it is necessary to have listeners for both ACL and BLE connect events
 *   - hearable ID, sensor send status, voice memo record feature and battery level all have to be
 *     updated on connection status change
 *   - request to send hearable sensor data to NEC backend can only be made after BLE connection
 *     (re-)established
 * And the monkey wrench in the works is the MQTT connection; if that goes away, BLE connection
 * won't matter because sensor data won't be able to be sent.
 *
 * So the conclusion is that the sanest, most reliable (and coincidentally simplest) approach is
 * simply to detect BLE disconnect event, pop up a dialog and take user back to landing page; this
 * will force re-establishment of MQTT and BLE to target hearable and ensure all features are working
 * properly when user returns to "home" screen
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class HearableDisconnectedDialog : DialogFragment() {
    private lateinit var viewModel: ViewModelCommon

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        }
            ?: throw Exception("DisconnectedDialog::onCreateDialog(): ERROR: Invalid Activity")

        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.home_hearable_connection_lost_title)
                .setMessage(R.string.home_hearable_connection_lost_descr)
                .setPositiveButton(
                    R.string.button_ok,
                    DialogInterface.OnClickListener { _, _ ->
                        NavHostFragment.findNavController(this@HearableDisconnectedDialog)
                            .navigate(
                                HearableDisconnectedDialogDirections.actionNavDialogHearableDisconnectedToNavLoginOrRegister()
                            )
                    })
            // Create the AlertDialog object and return it
            builder.create()
        }
            ?: throw IllegalStateException("DisconnectedDialog.onCreateDialog(): Activity cannot be null")
    }
}
