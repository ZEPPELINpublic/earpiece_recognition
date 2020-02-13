package jp.co.zeppelin.nec.hearable.domain.contract

interface IDeviceIdContract {

    /**
     * Scanned QR Code basic sanity check(s)
     *
     * Note: fail early and often -- throw if QR Code invalid
     */
    fun isQrCodeValid(qrCodeFormat: String?): Boolean

    /**
     * Multi-value function return
     *
     * @param isValid  is hearable device ID valid?
     * @param validId  valid ID from raw ID (i.e. truncated to correct length), can be used to
     *                 modify the value entered by user to strip extra characters etc
     */
    data class DeviceIdViaKeyboardResult(
        val isValid: Boolean,
        val validId: String
    )

    /**
     * For UI on-the-fly text input check (e.g. to enable "Done" button)
     *
     * Note: "User input is never an error:" don't throw, this is simply a graceful sanity check;
     * like credit card numbers we can't know if the string is an actual valid ID, but can at least
     * heuristically check it meets the basic criteria (string length, character types etc)
     */
    fun hasUserEnteredPossiblyValidDeviceId(deviceId: String): DeviceIdViaKeyboardResult

    /**
     * Retrieve device ID from scanned QR Code
     *
     * Note: fail early and often -- throw if parse fails
     */
    fun parseDeviceIdFromQrCode(qrCodeBlob: String?): String

    fun macAddressFromDeviceId(deviceIdRaw: String?): String
    fun deviceIdFromMacAddress(macAddress: String?): String
}
