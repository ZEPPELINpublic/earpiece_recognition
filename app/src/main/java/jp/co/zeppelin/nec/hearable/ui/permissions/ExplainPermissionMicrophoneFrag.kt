package jp.co.zeppelin.nec.hearable.ui.permissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.permissions.IPermissionsContract
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommon
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommonFactory
import kotlinx.android.synthetic.main.frag_explain_permission_microphone.*

/**
 * Screen explaining to user why microphone permission required by app
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ExplainPermissionMicrophoneFrag : Fragment() {
    private val TAG = ExplainPermissionMicrophoneFrag::class.java.simpleName
    private lateinit var viewModel: ViewModelCommon

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_explain_permission_microphone, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity?.run {
            val viewModelFactory =
                ViewModelCommonFactory(this)
            ViewModelProvider(this, viewModelFactory)
                .get(ViewModelCommon::class.java)
        } ?: throw Exception("$TAG::onActivityCreated(): ERROR: Invalid Activity")

        viewModel.microphonePermissionStatus.observe(
            viewLifecycleOwner,
            Observer { permissionStatusSingle ->
                permissionStatusSingle.getContentIfNotHandled()?.let { permissionStatus ->
                    when (permissionStatus) {
                        IPermissionsContract.PermissionStatus.Granted ->
                            NavHostFragment.findNavController(this@ExplainPermissionMicrophoneFrag)
                                .navigate(ExplainPermissionMicrophoneFragDirections.actionNavExplainPermissionMicrophoneToNavHome())
                        IPermissionsContract.PermissionStatus.Denied,
                        IPermissionsContract.PermissionStatus.PermanentlyDenied -> {
                            ZepLog.i(
                                TAG,
                                "onActivityCreated(): microphone permission denied (possibly permanently)"
                            )
                            viewModel.checkPermissionOrTakeToSettingsIfPermanentlyDeniedMicrophone()
                        }
                    }
                }
            })

        buttonNext.setOnClickListener {
            viewModel.checkPermissionOrTakeToSettingsIfPermanentlyDeniedMicrophone()
        }
    }

    override fun onStart() {
        super.onStart()
        // VoiceMemo feature depends on microphone permission
        viewModel.checkAndRequestPermissionMicrophoneForVoiceMemoRecording()
    }
}
