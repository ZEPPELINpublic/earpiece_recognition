package jp.co.zeppelin.nec.hearable.voicememo

import androidx.lifecycle.LiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent

/**
 * Top level contract I/F for voiceMemo recording
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface IVoiceMemoContract {
    fun startRecording(autoStopSecs: Double)

    fun stopRecording()

    // Observeable when recording has been cancelled following calling cancel-recording-request method below
    val cancelRecording: LiveData<SingleLiveDataEvent<Boolean>>

    // Request cancel recording; result will be emitted by above liveData
    fun cancelRecording()

    @kotlinx.coroutines.InternalCoroutinesApi
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun voiceMemoRecordTimer(recordTimeSecs: Long): ColdFlowCountdown
}
