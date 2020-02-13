package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

/**
 * Sealed class representing IHearableResultListener<Boolean> NEC hearable SDK responses
 * i.e. "success" returns just a boolean and "failed" returns status and exception
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class NecHearableSimpleBase(
    val isOK: Boolean,
    // empty or error condition details
    val msg: String
)

class NecHearableSimpleSuccess(
    isOK: Boolean,
    msg: String
) : NecHearableSimpleBase(isOK = isOK, msg = msg)

class NecHearableSimpleFailure(
    val errorCode: Int,
    msg: String
) : NecHearableSimpleBase(isOK = false, msg = msg)
