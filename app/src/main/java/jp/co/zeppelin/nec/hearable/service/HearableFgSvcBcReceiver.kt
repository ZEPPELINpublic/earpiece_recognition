package jp.co.zeppelin.nec.hearable.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import jp.co.zeppelin.nec.hearable.helper.AlertHelper

/**
 * Deal with restrictions introduced in API 26 ("Oreo") regarding background apps doing things in the
 * background
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class HearableFgSvcBcReceiver: BroadcastReceiver() {

    fun getFilter(): IntentFilter? {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        return filter
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action

        when (action) {
            Intent.ACTION_SCREEN_ON -> AlertHelper.makeToast(context, "onReceive()::action: ACTION_SCREEN_ON", true)
            Intent.ACTION_SCREEN_OFF -> AlertHelper.makeToast(context, "onReceive()::action: ACTION_SCREEN_OFF", true)
            Intent.ACTION_USER_PRESENT -> AlertHelper.makeToast(context, "onReceive()::action: ACTION_USER_PRESENT", true)
            Intent.ACTION_BOOT_COMPLETED -> AlertHelper.makeToast(context, "onReceive()::action: ACTION_BOOT_COMPLETED", true)
            else -> {}
        }
    }
}
