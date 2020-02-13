package jp.co.zeppelin.nec.hearable.voicememo

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.voicememo.model.VoiceMemoRecordProgress
import jp.co.zeppelin.nec.hearable.voicememo.model.VoiceMemoRecordTimeout
import jp.co.zeppelin.nec.hearable.voicememo.model.VoiceMemoRecordTimerBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

/**
 * Kotlin coroutine "Flow" with timeout
 *
 * Note: very similar to class ColdFlowWTimeout, but timer ticks intended for wall clock seconds
 * rather than progress bar advancement
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ColdFlowCountdown(
    recordTimeSecs: Long,
    val tag: String,
    val actionBegin: () -> Unit,
    val actionOnTimeout: () -> Unit
) {
    private val TAG = ColdFlowCountdown::class.java.simpleName

    var isCancelled = false

    val flow = (0..recordTimeSecs)
        .asFlow()
        .onEach { delay(1000) }
        .transform { elapsedSecs ->
            if (!isCancelled) {
                if (elapsedSecs < recordTimeSecs) {
                    emit(
                        VoiceMemoRecordProgress(
                            elapsedSecs
                        )
                    )
                } else {
                    emit(VoiceMemoRecordTimeout)
                }
            } else {
//                ZepLog.d(TAG, "$tag::ColdFlowWTimeout.observable: cancelled, skipping emit...")
            }
        }

    fun start(): LiveData<VoiceMemoRecordTimerBase> {
        return liveData(Dispatchers.IO) {
            actionBegin()
            flow
                .collect { elapsedSecs ->
                    if (!isCancelled) {
                        when (elapsedSecs) {
                            is VoiceMemoRecordTimeout -> {
                                ZepLog.d(
                                    TAG,
                                    "$tag::startObservableTimer()::VoiceMemoRecordTimeout"
                                )
                                actionOnTimeout()
                            }
                            is VoiceMemoRecordProgress -> {
//                                ZepLog.d(TAG, "$tag::progress pct: ${progressOrTimeout.progressPct}")
                            }
                        }
                        emit(elapsedSecs)
                    }
                }
        }
    }
}

