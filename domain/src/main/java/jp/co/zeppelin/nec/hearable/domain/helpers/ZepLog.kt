package jp.co.zeppelin.nec.hearable.domain.helpers

import android.util.Log

/**
 * Crude log indirection to allow silencing specific log levels
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
object ZepLog {
    private const val ZEP_LOG_LEVEL = Log.WARN

    fun v(tag: String, msg: String) {
        if (ZEP_LOG_LEVEL <= Log.VERBOSE) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (ZEP_LOG_LEVEL <= Log.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (ZEP_LOG_LEVEL <= Log.INFO) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (ZEP_LOG_LEVEL <= Log.WARN) {
            Log.w(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (ZEP_LOG_LEVEL <= Log.ERROR) {
            Log.e(tag, msg)
        }
    }

    fun wtf(tag: String, msg: String) {
        if (ZEP_LOG_LEVEL <= Log.ASSERT) {
            Log.wtf(tag, msg)
        }
    }
}
