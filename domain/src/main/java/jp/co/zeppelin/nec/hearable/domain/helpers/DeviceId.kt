package jp.co.zeppelin.nec.hearable.domain.helpers

import jp.co.zeppelin.nec.hearable.domain.contract.IDeviceIdContract
import kotlin.math.min

/**
 * NEC hearable device id is in fact simply a MAC address without the colons
 */
object DeviceId : IDeviceIdContract {
    private val TAG = DeviceId::class.java.simpleName
    // e.g. 000F9870ABCD
    private const val expectedQrCodeFormat = "QR_CODE"
    private const val deviceIdCharacterCount = 12
    private const val invalidMacAddr = ""

    enum class QrCodeItems {
        ModelNumber,
        SerialNumber,
        DeviceId
    }

    override fun isQrCodeValid(qrCodeFormat: String?): Boolean {
        if (qrCodeFormat == null) {
            throw AssertionError("isQrCodeValid(): ERROR: QR Code format null!")
        }
        return qrCodeFormat == expectedQrCodeFormat
    }

    override fun hasUserEnteredPossiblyValidDeviceId(deviceId: String): IDeviceIdContract.DeviceIdViaKeyboardResult =
        IDeviceIdContract.DeviceIdViaKeyboardResult(
            isValid = deviceId.length >= deviceIdCharacterCount,
            validId = deviceId.slice(0 until min(deviceId.length, 12))
        )

    override fun parseDeviceIdFromQrCode(qrCodeBlob: String?): String {
        if (qrCodeBlob == null) {
            throw AssertionError("parseDeviceIdFromQrCode(): ERROR: QR Code payload null!")
        }
        val qrCodePayload = qrCodeBlob.split(" ")
        if (qrCodePayload.size != 3) {
            throw AssertionError("Unexpected QR Code contents |$qrCodeBlob|, parse failed!")
        }
        val deviceId = qrCodePayload[QrCodeItems.DeviceId.ordinal]
        ZepLog.d(
            TAG,
            "parseDeviceIdFromQrCode(): devide id $deviceId parsed from QR Code payload |$qrCodeBlob|"
        )
        return deviceId
    }

    override fun macAddressFromDeviceId(deviceIdRaw: String?): String {
        // Gracefully handle accidental call with MAC addr
        val deviceId = deviceIdRaw?.replace(":", "")
        if (deviceId?.length != deviceIdCharacterCount) {
            return invalidMacAddr
        }
        val sb = StringBuilder()
        for (idx in deviceId.indices) {
            if (idx > 0 && idx % 2 == 0) {
                sb.append(":")
            }
            sb.append(deviceId[idx])
        }
        return sb.toString()
    }

    override fun deviceIdFromMacAddress(macAddress: String?): String {
        val deviceId = macAddress?.replace(":", "")
        return if (deviceId?.length != deviceIdCharacterCount) {
            invalidMacAddr
        } else {
            deviceId
        }
    }
}
