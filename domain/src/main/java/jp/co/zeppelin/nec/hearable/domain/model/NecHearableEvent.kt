package jp.co.zeppelin.nec.hearable.domain.model

/**
 * Sealed class representing NEC hearable SDK exceptional events
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class NecHearableEventBase

data class NecHearableSdkError(
    val msg: String
) : NecHearableEventBase()

