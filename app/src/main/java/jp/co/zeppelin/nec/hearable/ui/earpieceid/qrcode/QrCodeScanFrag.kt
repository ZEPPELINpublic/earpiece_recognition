package jp.co.zeppelin.nec.hearable.ui.earpieceid.qrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.google.zxing.Result
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import kotlinx.android.synthetic.main.frag_qr_code_scan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

/**
 * Screen responsible for scanning NEC Hearable QR code
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class QrCodeScanFrag : BaseVmFrag(),
    ZXingScannerView.ResultHandler {
    private val TAG = QrCodeScanFrag::class.java.simpleName

    // I/F ZXingScannerView.ResultHandler
    override fun handleResult(rawResult: Result?) {
        if (DeviceId.isQrCodeValid(rawResult?.barcodeFormat?.toString())) {
            viewModel.updateTargetDeviceId(DeviceId.parseDeviceIdFromQrCode(rawResult?.text))
            val action =
                QrCodeScanFragDirections.actionNavQrCodeScanToNavConfirmHearableId()
            action.didScanQrCode = true
            NavHostFragment.findNavController(this@QrCodeScanFrag)
                .navigate(action)
        } else {
            ZepLog.e(
                TAG,
                "handleResult(): ERROR: text: |${rawResult?.text}|, BC fmt: |${rawResult?.barcodeFormat}|, BC fmt name: |${rawResult?.barcodeFormat?.name}|"
            )
            textViewScanQR.text = resources.getString(
                R.string.format_qrcode_scan_error,
                rawResult?.barcodeFormat?.toString() ?: "(null result)"
            )
            NavHostFragment.findNavController(this@QrCodeScanFrag)
                .navigate(QrCodeScanFragDirections.actionNavQrCodeScanToNavQrCodePrep())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_qr_code_scan, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewButtonKeyboard.setOnClickListener {
            NavHostFragment.findNavController(this@QrCodeScanFrag)
                .navigate(QrCodeScanFragDirections.actionNavQrCodeScanToNavEarpieceIdViaKeyboard())
        }
    }

    override fun onStart() {
        super.onStart()
        // Ref: https://github.com/dm77/barcodescanner/issues/153
        zXingScannerViewQr.setResultHandler(this)
        zXingScannerViewQr.startCamera()
    }

    override fun onResume() {
        super.onResume()
        // Ref: https://github.com/dm77/barcodescanner/issues/153
        zXingScannerViewQr.resumeCameraPreview(this)
    }

    override fun onStop() {
        // Ref: https://github.com/dm77/barcodescanner/issues/153
        zXingScannerViewQr.stopCameraPreview()
        zXingScannerViewQr.stopCamera()

        super.onStop()
    }
}
