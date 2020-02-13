package jp.co.zeppelin.nec.hearable.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import jp.co.zeppelin.nec.hearable.R

/**
 * Top level impl for shared user prefs
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class SettingsFrag : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.nec_hearable_preferences, rootKey)
    }
}
