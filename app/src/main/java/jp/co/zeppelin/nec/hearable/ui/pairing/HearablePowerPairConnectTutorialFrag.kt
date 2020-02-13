package jp.co.zeppelin.nec.hearable.ui.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.vm.BaseVmFrag
import kotlinx.android.synthetic.main.frag_check_hearable_power_pair_connect.*

/**
 * Screen displayed when target hearable device not bluetooth-classic-paired-and-connected
 * and user may need guidance getting this condition established
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class HearablePowerPairConnectTutorialFrag : BaseVmFrag() {
    private val TAG = HearablePowerPairConnectTutorialFrag::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_check_hearable_power_pair_connect, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonNext.setOnClickListener {
            val action =
                HearablePowerPairConnectTutorialFragDirections.actionNavHearablePowerPairConnectTutorialToNavBluetoothSystemPairingTutorial()
            action.doAlreadyKnowHearableId = true
            NavHostFragment.findNavController(this@HearablePowerPairConnectTutorialFrag)
                .navigate(action)
        }
    }
}
