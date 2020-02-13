package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

/**
 * Repr for response from NEC Hearable SDK to "hearableMqttRequest.setMqttParameter()" request
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class NecHearableMqttBase(
    val isOK: Boolean,
    val status: Int,
    // empty or error condition details
    val msg: String
)

class NecHearableMqttSuccess(
    isOK: Boolean,
    status: Int,
    msg: String
) : NecHearableMqttBase(isOK = isOK, status = status, msg = msg)

class NecHearableMqttFail(
    status: Int,
    msg: String
) : NecHearableMqttBase(isOK = false, status = status, msg = msg)
