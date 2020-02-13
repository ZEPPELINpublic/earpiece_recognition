package jp.co.zeppelin.nec.hearable.ui.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.permissions.IPermissionsContract
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommon
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommonFactory

/**
 * Screen explaining to user why location permission required by app
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ExplainPermissionLocationFrag : Fragment() {
    private val TAG = ExplainPermissionLocationFrag::class.java.simpleName
    private lateinit var viewModel: ViewModelCommon
    private lateinit var buttonNext: Button

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        } ?: throw Exception("$TAG::onActivityCreated(): ERROR: Invalid Activity")

        viewModel.locationPermissionGranted.observe(
            viewLifecycleOwner,
            Observer { permissionStatusSingle ->
                permissionStatusSingle.getContentIfNotHandled()?.let { permissionStatus ->
                    when (permissionStatus) {
                        IPermissionsContract.PermissionStatus.Granted ->
                            NavHostFragment.findNavController(this@ExplainPermissionLocationFrag)
                                .navigate(ExplainPermissionLocationFragDirections.actionNavPermissionLocationExplainToNavConfirmHearableId())
                        IPermissionsContract.PermissionStatus.Denied,
                        IPermissionsContract.PermissionStatus.PermanentlyDenied -> {
                            ZepLog.i(
                                TAG,
                                "onActivityCreated(): location permission denied (possibly permanently)"
                            )
                            viewModel.checkPermissionOrTakeToSettingsIfPermanentlyDeniedLocation()
                        }
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.frag_explain_permission_location, container, false)
        buttonNext = root.findViewById(R.id.buttonNext)
        buttonNext.setOnClickListener {
            viewModel.checkPermissionOrTakeToSettingsIfPermanentlyDeniedLocation()
        }
        return root
    }

    override fun onStart() {
        super.onStart()
        // Check case of user navigating back from system settings after granting permission
        viewModel.checkAndRequestPermissionsBluetoothNLocation()
    }
}
