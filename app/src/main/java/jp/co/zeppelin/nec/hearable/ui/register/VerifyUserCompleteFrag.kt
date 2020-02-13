package jp.co.zeppelin.nec.hearable.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_complete_ok.*

/**
 * Screen displayed when user verification complete
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class VerifyUserCompleteFrag : BaseLocationPermissionFrag() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_complete_ok, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.apply {
            // Reset system top status bar color from previous screen using themed color override
            this.window?.statusBarColor = 0
        }

        buttonNext.setOnClickListener {
            NavHostFragment.findNavController(this@VerifyUserCompleteFrag)
                .navigate(VerifyUserCompleteFragDirections.actionNavVerifyUserCompleteToNavHome())
        }
    }
}
