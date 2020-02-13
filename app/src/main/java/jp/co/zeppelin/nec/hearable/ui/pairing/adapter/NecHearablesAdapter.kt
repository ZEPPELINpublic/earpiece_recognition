package jp.co.zeppelin.nec.hearable.ui.pairing.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.model.data.HearableAssoc
import jp.co.zeppelin.nec.hearable.domain.model.data.HearableVerified
import jp.co.zeppelin.nec.hearable.ui.vm.ViewModelCommon
import kotlinx.android.synthetic.main.item_nec_hearable.view.*

/**
 * Adapter for displaying list of Hearable devices to user for selection
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class NecHearablesAdapter(
    var dataItems: MutableList<HearableAssoc>,
    private val viewModel: ViewModelCommon
) : RecyclerView.Adapter<NecHearablesAdapter.ViewHolder>(),
    IHearablesAdapterContract {
    private val TAG = NecHearablesAdapter::class.java.simpleName

    val hearableSelectedInList_ = MutableLiveData<SingleLiveDataEvent<HearableVerified>>()
    override val hearableSelectedInList = hearableSelectedInList_

//    private var dataItems: List<HearableAssoc> = listOf()

    fun updateData(items: List<HearableAssoc>) {
        dataItems = items.toMutableList()
        updateData()
    }

    fun updateData() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nec_hearable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataItems[position]
        holder.deviceAddress.text = item.hearableId.hearableIdStr
        val hearableVerified = item.toHearableVerified()
        holder.niceUsername.text = hearableVerified.niceUsername
        holder.wearerId.text = hearableVerified.wearerId
        if (item.hearableId.hearableIdStr == viewModel.lastTargetDeviceId()) {
            holder.radioButton.isChecked = true
            hearableSelectedInList_.postValue(SingleLiveDataEvent(hearableVerified))
        } else {
            holder.radioButton.isChecked = false
        }
        with(holder.view) {
            tag = item
        }
    }

    override fun getItemCount(): Int = dataItems.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val niceUsername: TextView = view.textViewNiceUsername
        val wearerId: TextView = view.textViewWearerId
        val deviceAddress: TextView = view.textViewNecHearableMACAddress
        val radioButton: RadioButton = view.radioButtonSelectItem

        // Ref: https://github.com/mutexkid/radiogroup-recyclerview/blob/master/app/src/main/java/com/bignerdranch/radiotest/RadioAdapter.java
        init {
            val onClickListener = View.OnClickListener {
                viewModel.setLastTargetDeviceId(DeviceId.deviceIdFromMacAddress(dataItems[adapterPosition].hearableId.hearableIdStr))
                notifyItemRangeChanged(0, dataItems.size)
                hearableSelectedInList_.postValue(SingleLiveDataEvent(dataItems[adapterPosition].toHearableVerified()))
            }
            radioButton.setOnClickListener(onClickListener)
            view.setOnClickListener(onClickListener)
        }
    }
}
