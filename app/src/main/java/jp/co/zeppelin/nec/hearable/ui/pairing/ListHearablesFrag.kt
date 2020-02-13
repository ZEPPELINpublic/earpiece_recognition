package jp.co.zeppelin.nec.hearable.ui.pairing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.PairedAndConnected
import jp.co.zeppelin.nec.hearable.ui.pairing.adapter.NecHearablesAdapter
import jp.co.zeppelin.nec.hearable.ui.permissions.base.BaseLocationPermissionFrag
import kotlinx.android.synthetic.main.frag_list_hearables.*

/**
 * Screen displaying list of hearables which have already successfully registered and verified
 * by NEC backend servers, so returning users ("Log in" flow) can quickly select device from list
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ListHearablesFrag : BaseLocationPermissionFrag() {
    private val TAG = ListHearablesFrag::class.java.simpleName

    var isHearablePairedAndConnectedViaBluetooth = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.frag_list_hearables, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageViewButtonCamera.setOnClickListener {
            NavHostFragment.findNavController(this@ListHearablesFrag)
                .navigate(ListHearablesFragDirections.actionNavHearablesListToNavQrCodePrep())
        }

        textViewButtonHearableNotShown.setOnClickListener {
            NavHostFragment.findNavController(this@ListHearablesFrag)
                .navigate(ListHearablesFragDirections.actionNavListHearablesToNavHearablePowerPairConnectTutorial())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonConnectDevice.isEnabled = false

        viewModel.checkTargetDevicePairedAndConnected()

        viewModel.targetDeviceBluetoothConnectionStatus.observe(
            viewLifecycleOwner,
            Observer { bluetoothMsgBaseSingle ->
                bluetoothMsgBaseSingle.getContentIfNotHandled()
                    ?.let { bluetoothPaireAndConnectedResult ->
                        isHearablePairedAndConnectedViaBluetooth =
                            bluetoothPaireAndConnectedResult is PairedAndConnected
                    }
            })

        viewModel.registeredHearableDatasetsFromDB.observe(
            viewLifecycleOwner,
            Observer { hearableDatasets ->
                ZepLog.e(TAG, "onActivityCreated():observer.hearableDatasets: $hearableDatasets")
                with(recyclerViewPairableDevices) {
                    layoutManager = LinearLayoutManager(context)
                    adapter = NecHearablesAdapter(hearableDatasets.toMutableList(), viewModel)
                    (adapter as NecHearablesAdapter).hearableSelectedInList.observe(
                        viewLifecycleOwner,
                        Observer { respObjSgl ->
                            respObjSgl.getContentIfNotHandled()?.let { _ ->
                                buttonConnectDevice.isEnabled = true
                            }
                        })
                }
            })

        buttonConnectDevice.setOnClickListener {
            viewModel.updateTargetDeviceId(viewModel.lastTargetDeviceId())
            if (isHearablePairedAndConnectedViaBluetooth) {
                NavHostFragment.findNavController(this@ListHearablesFrag)
                    .navigate(ListHearablesFragDirections.actionNavListHearablesToNavConnectViaBle())
            } else {
                NavHostFragment.findNavController(this@ListHearablesFrag)
                    .navigate(ListHearablesFragDirections.actionNavListHearablesToNavDialogHearableNotBothBtClassicPairedNConnected())
            }
        }
    }
}
