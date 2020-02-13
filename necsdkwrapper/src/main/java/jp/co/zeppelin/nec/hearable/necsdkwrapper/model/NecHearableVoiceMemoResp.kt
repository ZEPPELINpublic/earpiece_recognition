package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

/**
 * NEC Hearable voice memo response observables
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */

/**
 * Result of attempting to record voice memo to local .wav file
 */
sealed class NecHearableVoiceMemoRecordRespBase

class NecHearableVoiceMemoRecordSuccess : NecHearableVoiceMemoRecordRespBase()

class NecHearableVoiceMemoRecordFail : NecHearableVoiceMemoRecordRespBase()

/**
 * Result of attempting upload of local voice memo recorded .wav file to server
 */
sealed class NecHearableVoiceMemoUploadRespBase

class NecHearableVoiceMemoUploadSuccess : NecHearableVoiceMemoUploadRespBase()

class NecHearableVoiceMemoUploadFail : NecHearableVoiceMemoUploadRespBase()
