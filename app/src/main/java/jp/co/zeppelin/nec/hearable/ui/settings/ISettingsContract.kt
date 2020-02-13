package jp.co.zeppelin.nec.hearable.ui.settings

/**
 * Top level contract I/F for shared user prefs
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface ISettingsContract {
    /**
     * Persist target hearable device id (MAC address), from either QR code scan or keyboard entry
     */
    fun lastTargetDeviceId(): String

    fun setLastTargetDeviceId(deviceId: String)
}
