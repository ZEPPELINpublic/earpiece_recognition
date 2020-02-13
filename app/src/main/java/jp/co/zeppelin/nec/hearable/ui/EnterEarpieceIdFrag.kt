package jp.co.zeppelin.nec.hearable.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseBluetoothCheckFrag
import kotlinx.android.synthetic.main.frag_enter_earpiece_id.*

/**
 * Enter earpiece hardware ID, either by manually typing in the ID number printed on the attached
 * label, or by using device camera to scan QR code
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class EnterEarpieceIdFrag : BaseBluetoothCheckFrag() {
    private val TAG = EnterEarpieceIdFrag::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_enter_earpiece_id, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewButtonQrCode.setOnClickListener {
            NavHostFragment.findNavController(this@EnterEarpieceIdFrag)
                .navigate(EnterEarpieceIdFragDirections.actionEnterEarpieceIdFragToQrCodeScanPrepFrag())
        }
        viewButtonKeyboard.setOnClickListener {
            NavHostFragment.findNavController(this@EnterEarpieceIdFrag)
                .navigate(EnterEarpieceIdFragDirections.actionEnterEarpieceIdFragToNavEarpieceIdViaKeyboard())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            // Reset system top status bar color in case previous screen using themed color override
            this.window?.statusBarColor = 0
        }
    }
}
