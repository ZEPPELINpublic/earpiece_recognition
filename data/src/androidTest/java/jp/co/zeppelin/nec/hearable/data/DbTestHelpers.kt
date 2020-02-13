package jp.co.zeppelin.nec.hearable.data

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking


object DbTestHelpers {
    private val TAG = DbTestHelpers::class.java.simpleName

    val hearableAMacAddr = "00:0A:45:10:DF:9A"
    val hearableAId = "000A4510DF9A"
    val hearableAWearerId = "79476dfb-7ff9-4ec8-87fa-d87d85ccdcaf"
    val hearableANiceUsername = "GlenGaryGlennRoss"

    val hearableBMacAddr = "00:0A:45:10:DF:D8"
    val hearableBId = "000A4510DFD8"
    val hearableBWearerId = "8e378309-5eb4-4001-a106-a840ed43e249"
    val hearableBNiceUsername = "GuinessIsGoodForYou"

    val hearableCMacAddr = "00:0A:45:10:DF:41"
    val hearableCId = "000A4510DF41"
    val hearableCWearerId = "d0e31299-a038-4aba-8c25-446a60b36fd3"
    val hearableCNiceUsername = "TippicanoeAndTylerToo"

    fun waitForCoroJob(job: Job, isFinished: () -> Boolean): Int {
        var spinWaitCount = 0
        runBlocking {
            while (!isFinished()) {
                ++spinWaitCount
            }
            job.cancelAndJoin()
        }
        return spinWaitCount
    }
}
