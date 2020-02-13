package jp.co.zeppelin.nec.hearable.voicememo.model

/**
 * Sealed class representing voicememo record progress or timeout
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class VoiceMemoRecordTimerBase

data class VoiceMemoRecordProgress(
    val recordSecs: Long
) : VoiceMemoRecordTimerBase()

object VoiceMemoRecordTimeout : VoiceMemoRecordTimerBase()
