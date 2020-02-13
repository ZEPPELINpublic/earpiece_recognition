package jp.co.zeppelin.nec.hearable.necsdkwrapper.coro

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.ProgressUpdateEvent
import jp.co.zeppelin.nec.hearable.domain.model.ProgressWTimeoutBase
import jp.co.zeppelin.nec.hearable.domain.model.TimeoutEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

/**
 * Kotlin coroutine "Flow" with timeout
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@kotlinx.coroutines.InternalCoroutinesApi
@kotlinx.coroutines.ExperimentalCoroutinesApi
class ColdFlowWTimeout(
    timeoutSec: Long,
    val tag: String,
    val actionBegin: suspend () -> Unit,
    val actionOnTimeout: () -> Unit
) {
    private val TAG = ColdFlowWTimeout::class.java.simpleName

    var isCancelled = false

    /**
     * Emit progress bar update ticks so that progress will go from 0 to 100 in timeoutSec seconds
     */
    private fun delayMillisForSeconds(timeoutSec: Long): Long = timeoutSec * 10

    val flow = (0..101)
        .asFlow()
        .onEach { delay(delayMillisForSeconds(timeoutSec)) }
        .transform { progressPct ->
            if (!isCancelled) {
                if (progressPct < 101) {
                    emit(
                        ProgressUpdateEvent(
                            progressPct
                        )
                    )
                } else {
                    emit(TimeoutEvent)
                }
            } else {
//                ZepLog.i(TAG, "$tag::ColdFlowWTimeout.observable: cancelled, skipping emit...")
            }
        }

    fun start(): LiveData<ProgressWTimeoutBase> {
        return liveData(Dispatchers.IO) {
            actionBegin()
            flow
                .collect { progressOrTimeout ->
                    if (!isCancelled) {
                        when (progressOrTimeout) {
                            is TimeoutEvent -> {
                                ZepLog.i(TAG, "$tag::startObservableTimer()::TimeoutEvent")
                                actionOnTimeout()
                            }
                            is ProgressUpdateEvent -> {
//                                ZepLog.d(TAG, "$tag::progress pct: ${progressOrTimeout.progressPct}")
                            }
                        }
                        emit(progressOrTimeout)
                    }
                }
        }
    }
}
