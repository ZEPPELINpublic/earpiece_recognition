package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_complete_ok_anim.*

/**
 * Screen indicating successful registration via ear biometric
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class RegistrationCompleteFrag : BaseLocationPermissionFrag() {
    private val TAG = RegistrationCompleteFrag::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_complete_ok_anim, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            this.window?.statusBarColor =
                ContextCompat.getColor(this.applicationContext, R.color.play_sound_alt_theme_purple)
        }

        buttonNext.isEnabled = false
        // Only on registration complete are we allowed to persist the wearerID from the server, so
        // defer persisting everything (hearable ID, wearer ID, "nice" username) until the same moment
        viewModel.hearableDatasetInsertedInDB.observe(
            viewLifecycleOwner,
            Observer { hearableWearerUsernameIds ->
                ZepLog.e(
                    TAG,
                    "hearableWearerIdPair::observe: hearable PK: ${hearableWearerUsernameIds.hearableId}, wearable PK: ${hearableWearerUsernameIds.wearerId}, username PK: ${hearableWearerUsernameIds.niceUsernameId}"
                )
                // Next, mark the newly inserted object as authenticated
                viewModel.hearableRegistered.observe(viewLifecycleOwner, Observer { _ ->
                    buttonNext.isEnabled = true
                })
            })

        buttonNext.setOnClickListener {
            NavHostFragment.findNavController(this@RegistrationCompleteFrag)
                .navigate(RegistrationCompleteFragDirections.actionNavRegistrationCompleteToNavInsertEarpieceConfirm())
        }
    }
}
