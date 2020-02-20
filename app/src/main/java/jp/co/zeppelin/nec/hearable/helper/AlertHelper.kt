package jp.co.zeppelin.nec.hearable.helper

import android.content.Context
import android.widget.Toast

/**
 * Helper for making android "toast" popups
 *
 * By setting single variable DO_ENABLE_TOASTS all toasts can easily be enabled/disabled
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
object AlertHelper {

    // In response to NEC request to diable all toasts (7 Feb 2020)
    const val DO_ENABLE_TOASTS = false

    fun makeToast(context: Context?, msg: String, doForceShowToast: Boolean= DO_ENABLE_TOASTS) = context.apply {
        if (doForceShowToast && msg.isNotEmpty()) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
