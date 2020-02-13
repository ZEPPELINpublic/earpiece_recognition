package jp.co.zeppelin.nec.hearable.ui.earpieceid.qrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.permissions.IPermissionsContract
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import kotlinx.android.synthetic.main.frag_qr_code_prep.*

/**
 * Screen displayed immediately before QR code scan screen
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class QrCodeScanPrepFrag : BaseVmFrag() {
    private val TAG = QrCodeScanPrepFrag::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_qr_code_prep, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonNext.setOnClickListener {
            if (!viewModel.isPermissionCameraGranted()) {
                viewModel.checkAndRequestPermissionCameraForQRCodeScanning()
            } else {
                NavHostFragment.findNavController(this@QrCodeScanPrepFrag)
                    .navigate(R.id.action_qrCodeScanPrepFrag_to_qrCodeScanFrag)
            }
        }

        viewModel.cameraPermissionStatus.observe(
            viewLifecycleOwner,
            Observer { permissionStatusSingle ->
                permissionStatusSingle.getContentIfNotHandled()?.let { permissionStatus ->
                    when (permissionStatus) {
                        IPermissionsContract.PermissionStatus.Granted -> {
                            NavHostFragment.findNavController(this@QrCodeScanPrepFrag)
                                .navigate(QrCodeScanPrepFragDirections.actionQrCodeScanPrepFragToQrCodeScanFrag())
                        }
                        IPermissionsContract.PermissionStatus.Denied -> {
                            ZepLog.i(
                                TAG,
                                "onActivityCreated(): camera permission denied"
                            )
                            NavHostFragment.findNavController(this@QrCodeScanPrepFrag)
                                .navigate(QrCodeScanPrepFragDirections.actionNavQrCodePrepToNavPermissionCameraExplain())
                        }
                        IPermissionsContract.PermissionStatus.PermanentlyDenied -> {
                            NavHostFragment.findNavController(this@QrCodeScanPrepFrag)
                                .navigate(QrCodeScanPrepFragDirections.actionGlobalNavSettings())
                            ZepLog.i(
                                TAG,
                                "onActivityCreated(): camera permission permanently denied"
                            )
                        }
                    }
                }
            })
    }
}
