package jp.co.zeppelin.nec.hearable.ui.pairing.adapter

import androidx.lifecycle.LiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.model.data.HearableVerified

/**
 * Top level contract I/F for adapter for list of verified hearables
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface IHearablesAdapterContract {
    val hearableSelectedInList: LiveData<SingleLiveDataEvent<HearableVerified>>
}
