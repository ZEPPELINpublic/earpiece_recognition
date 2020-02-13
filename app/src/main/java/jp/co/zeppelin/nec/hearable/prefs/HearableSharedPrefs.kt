package jp.co.zeppelin.nec.hearable.prefs

import android.content.Context
import androidx.preference.PreferenceManager
import jp.co.zeppelin.nec.hearable.R
import jp.co.zeppelin.nec.hearable.ui.settings.ISettingsContract

/**
 * Android shared preference
 *
 * For persisting simple data across app restarts as usual, without resorting to full-on DB
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class HearableSharedPrefs(context: Context) : ISettingsContract {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val keyTargetDeviceId =
        context.resources.getString(R.string.key_nec_hearable_target_device_id)

    override fun lastTargetDeviceId(): String =
        sharedPreferences.getString(keyTargetDeviceId, "") ?: ""

    override fun setLastTargetDeviceId(deviceId: String) =
        sharedPreferences.edit().putString(keyTargetDeviceId, deviceId).apply()
}
