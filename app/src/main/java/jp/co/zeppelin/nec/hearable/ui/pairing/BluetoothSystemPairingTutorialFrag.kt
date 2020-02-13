package jp.co.zeppelin.nec.hearable.ui.pairing

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.NotPairedAndConnected
import jp.co.zeppelin.nec.hearable.domain.model.PairedAndConnected
import jp.co.zeppelin.nec.hearable.helper.AnimHelper
import jp.co.zeppelin.nec.hearable.navigation.INavigationContract
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseBluetoothCheckFrag
import kotlinx.android.synthetic.main.frag_bluetooth_pairing.*

/**
 * Tutorial screen for pairing with hearable in android handset system bluetooth settings
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class BluetoothSystemPairingTutorialFrag : BaseBluetoothCheckFrag() {
    private val TAG = BluetoothSystemPairingTutorialFrag::class.java.simpleName

    val args: BluetoothSystemPairingTutorialFragArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_bluetooth_pairing, container, false)

    private fun observeIsPairedAndConnected() {
        viewModel.targetDeviceBluetoothConnectionStatus.observe(
            viewLifecycleOwner,
            Observer { bluetoothMsgBaseSingle ->
                bluetoothMsgBaseSingle.getContentIfNotHandled()
                    ?.let { bluetoothPaireAndConnectedResult ->
                        when (bluetoothPaireAndConnectedResult) {
                            is PairedAndConnected -> {
                                NavHostFragment.findNavController(this@BluetoothSystemPairingTutorialFrag)
                                    .navigate(BluetoothSystemPairingTutorialFragDirections.actionNavBluetoothSystemPairingTutorialToNavConnectViaBle())
                            }
                            is NotPairedAndConnected -> {
                                ZepLog.e(
                                    TAG,
                                    "targetDeviceBluetoothConnectionStatus::observer: WARNING: target device with MAC address ${bluetoothPaireAndConnectedResult.targetMacAddr} not discovered!"
                                )
                                NavHostFragment.findNavController(this@BluetoothSystemPairingTutorialFrag)
                                    .navigate(BluetoothSystemPairingTutorialFragDirections.actionNavBluetoothSystemPairingTutorialToNavLoginOrRegister())
                            }
                            else -> {
                                throw AssertionError("$TAG::targetDeviceBluetoothConnectionStatus::observer: ERROR: unexpected result! $bluetoothPaireAndConnectedResult (type ${bluetoothPaireAndConnectedResult.javaClass.simpleName})")
                            }
                        }
                    }
            })

    }

    /**
     * Take user to android handset system Bluetooth settings (pairing control etc)
     *
     * Note:
     *   intent has no output so startActivityForResult() is meaningless; this will take the user
     *   to settings but they'll need to restart this app themselves
     *   Ref: https://developer.android.com/reference/android/provider/Settings#ACTION_BLUETOOTH_SETTINGS
     *
     * Works on:
     *  - Samsung SC-02K running android 9 (API 28)
     *  - Sony SO-04K running android 9 (API 28)
     */
    private fun navigateToSystemBluetoothSettings() {
        val intentBluetooth = Intent()
        intentBluetooth.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        startActivity(intentBluetooth)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (args.doAlreadyKnowHearableId || viewModel.loginSignup == INavigationContract.LoginSignup.Signup) {
            // Display full information if actual connection difficulty or welcoming new user
            AnimHelper.pulseAnimFor(context, imageViewSystemSettingsBluetoothRect)
        } else {
            // Display less information when welcoming returning user
            textVewLabelPermissionExplainDescr.text =
                resources.getString(R.string.descr_bluetooth_pairing_tutorial_login_flow)
            imageViewSystemSettingsBluetooth.visibility = View.INVISIBLE
            imageViewSystemSettingsBluetoothRect.visibility = View.INVISIBLE
            imageViewSystemSettingsBluetoothLoginFlow.visibility = View.VISIBLE
            imageViewSystemSettingsBluetoothRectLoginFlow.visibility = View.VISIBLE
            textViewButtonBluetoothSettings.visibility = View.INVISIBLE
            AnimHelper.pulseAnimFor(context, imageViewSystemSettingsBluetoothRectLoginFlow)
        }

        textViewButtonBluetoothSettings.setOnClickListener {
            navigateToSystemBluetoothSettings()
        }

        observeIsPairedAndConnected()

        buttonNext.setOnClickListener {
            if (args.doAlreadyKnowHearableId) {
                // Actual hearable connection difficulty; next screen depends on whether user has resolved problem
                viewModel.checkTargetDevicePairedAndConnected()
            } else {
                // Okay, user duly edumacated, back to main nav flow
                when (viewModel.loginSignup) {
                    INavigationContract.LoginSignup.Login ->
                        NavHostFragment.findNavController(this@BluetoothSystemPairingTutorialFrag)
                            .navigate(BluetoothSystemPairingTutorialFragDirections.actionNavBluetoothSystemPairingTutorialToNavListHearables())
                    INavigationContract.LoginSignup.Signup ->
                        NavHostFragment.findNavController(this@BluetoothSystemPairingTutorialFrag)
                            .navigate(BluetoothSystemPairingTutorialFragDirections.actionNavBluetoothSystemPairingTutorialToNavEnterNiceName())
                }
            }
        }
    }
}
