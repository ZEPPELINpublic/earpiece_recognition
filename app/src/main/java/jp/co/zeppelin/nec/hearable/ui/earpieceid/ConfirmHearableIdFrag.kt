package jp.co.zeppelin.nec.hearable.ui.earpieceid

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
import jp.co.zeppelin.nec.hearable.permissions.IPermissionsContract
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_confirm_earpiece_id.*

/**
 * Screen displayed immediately after user scans QR code or enters Hearable ID via keyboard to
 * allow final verification before proceeding to BLE connection
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ConfirmHearableIdFrag : BaseLocationPermissionFrag() {
    private val TAG = ConfirmHearableIdFrag::class.java.simpleName

    val args: ConfirmHearableIdFragArgs by navArgs()

    var isLoginOrSignupFlowButtonReady = false
    var isHearablePairedAndConnecteViaBluetoothButtonReady = false
    var isHearablePairedAndConnecteViaBluetooth = false

    fun enableNextButton() {
        buttonNext.isEnabled =
            isHearablePairedAndConnecteViaBluetoothButtonReady && isLoginOrSignupFlowButtonReady
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_confirm_earpiece_id, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.didScanQrCode) {
            textViewButtonRescanQrCode.setOnClickListener {
                NavHostFragment.findNavController(this@ConfirmHearableIdFrag)
                    .navigate(ConfirmHearableIdFragDirections.actionNavConfirmHearableIdToNavQrCodeScan())
            }
        } else {
            textViewButtonRescanQrCode.visibility = View.GONE
        }

        AnimHelper.pulseAnimFor(context, imageViewEarpieceLabelHighlightRect)

        buttonNext.isEnabled = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        textViewHearableDeviceId.text = viewModel.lastTargetDeviceId()

        viewModel.checkTargetDevicePairedAndConnected()

        viewModel.targetDeviceBluetoothConnectionStatus.observe(
            viewLifecycleOwner,
            Observer { bluetoothMsgBaseSingle ->
                bluetoothMsgBaseSingle.getContentIfNotHandled()
                    ?.let { bluetoothPaireAndConnectedResult ->
                        when (bluetoothPaireAndConnectedResult) {
                            is PairedAndConnected -> {
                                isHearablePairedAndConnecteViaBluetooth = true
                            }
                            is NotPairedAndConnected -> {
                                ZepLog.i(
                                    TAG,
                                    "targetDeviceBluetoothConnectionStatus::observer: WARNING: target device with MAC address ${bluetoothPaireAndConnectedResult.targetMacAddr} not discovered!"
                                )
                                isHearablePairedAndConnecteViaBluetooth = false
                            }
                            else -> {
                                throw AssertionError("$TAG::targetDeviceBluetoothConnectionStatus::observer: ERROR: unexpected result! $bluetoothPaireAndConnectedResult (type ${bluetoothPaireAndConnectedResult.javaClass.simpleName})")
                            }
                        }
                        isHearablePairedAndConnecteViaBluetoothButtonReady = true
                        enableNextButton()
                    }
            })

        when (viewModel.loginSignup) {
            INavigationContract.LoginSignup.Login -> {
                viewModel.hearableDatasetFromDBForHearableID.observe(
                    viewLifecycleOwner,
                    Observer { respObjSgl ->
                        respObjSgl.getContentIfNotHandled()?.let { hearableAssocOneToOne ->
                            if (hearableAssocOneToOne.hearableId.isNotEmpty() && hearableAssocOneToOne.niceUsername.isNotEmpty()) {
                                viewModel.niceUsername = hearableAssocOneToOne.niceUsername
                            }
                            isLoginOrSignupFlowButtonReady = true
                            enableNextButton()
                        }
                    })
            }
            INavigationContract.LoginSignup.Signup -> {
                isLoginOrSignupFlowButtonReady = true
                enableNextButton()
            }
        }
        buttonNext.setOnClickListener {
            if (!viewModel.isPermissionLocationGranted()) {
                viewModel.checkAndRequestPermissionsBluetoothNLocation()
            } else {
                if (isHearablePairedAndConnecteViaBluetooth) {
                    NavHostFragment.findNavController(this@ConfirmHearableIdFrag)
                        .navigate(ConfirmHearableIdFragDirections.actionNavConfirmHearableIdToNavConnectViaBle())
                } else {
                    NavHostFragment.findNavController(this@ConfirmHearableIdFrag)
                        .navigate(ConfirmHearableIdFragDirections.actionNavConfirmHearableIdToNavDialogHearableNotBothBtClassicPairedNConnected())
                }
            }
        }

        viewModel.locationPermissionGranted.observe(
            viewLifecycleOwner,
            Observer { permissionStatusSingle ->
                permissionStatusSingle.getContentIfNotHandled()?.let { permissionStatus ->
                    when (permissionStatus) {
                        IPermissionsContract.PermissionStatus.Granted -> { /* No-op */
                        }
                        IPermissionsContract.PermissionStatus.Denied,
                        IPermissionsContract.PermissionStatus.PermanentlyDenied -> {
                            NavHostFragment.findNavController(this@ConfirmHearableIdFrag)
                                .navigate(ConfirmHearableIdFragDirections.actionNavConfirmHearableIdToNavPermissionLocationExplain())
                            ZepLog.i(
                                TAG,
                                "onActivityCreated(): location permission ${if (permissionStatus == IPermissionsContract.PermissionStatus.PermanentlyDenied) "permanently denied" else "denied"}"
                            )
                        }
                    }
                }
            })
    }
}
