package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

/**
 * Repr for (device local i.e. server/backend N/A) ear "feature" measurement response
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class NecHearableMeasureEarFeatRespBase(val didSucceed: Boolean)

class NecHearableMeasureEarFeatSuccess(
    didSucceed: Boolean
) : NecHearableMeasureEarFeatRespBase(didSucceed)

class NecHearableMeasureEarFeatFail(
    didSucceed: Boolean,
    val msg: String
) : NecHearableMeasureEarFeatRespBase(didSucceed)
