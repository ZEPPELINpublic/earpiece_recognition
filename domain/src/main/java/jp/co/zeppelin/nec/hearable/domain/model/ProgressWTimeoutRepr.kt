package jp.co.zeppelin.nec.hearable.domain.model

/**
 * Sealed class representing timer progress update or timeout
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class ProgressWTimeoutBase

data class ProgressUpdateEvent(
    val progressPct: Int
) : ProgressWTimeoutBase()

object TimeoutEvent : ProgressWTimeoutBase()
