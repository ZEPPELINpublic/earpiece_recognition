package jp.co.zeppelin.nec.hearable.ui.permissions.base

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.necsdkwrapper.constants.NecHearableSDKConstants
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag

/**
 * Base fragment which checks whether android handset system bluetooth radio is enabled
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
abstract class BaseBluetoothCheckFrag : BaseVmFrag() {
    private val TAG = BaseBluetoothCheckFrag::class.java.simpleName

    override fun onResume() {
        super.onResume()
        viewModel.updateBluetoothEnabledStatus()
        viewModel.isBluetoothEnabled.observe(
            viewLifecycleOwner,
            Observer { isBluetoothEnabledSingle ->
                isBluetoothEnabledSingle.getContentIfNotHandled()?.let { isBluetoothEnabled ->
                    if (!isBluetoothEnabled) {
                        // Ref: https://developer.android.com/guide/topics/connectivity/bluetooth-le
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        startActivityForResult(
                            enableBtIntent,
                            NecHearableSDKConstants.REQUEST_ENABLE_BT
                        )
                    }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            NecHearableSDKConstants.REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    NavHostFragment.findNavController(this@BaseBluetoothCheckFrag)
                        .navigate(R.id.nav_enter_earpiece_id)
                } else {
                    ZepLog.e(TAG, "onActivityResult(): WARNING: REQUEST_ENABLE_BT result failure!")
                }
            }
            else -> {
            }
        }
    }
}
