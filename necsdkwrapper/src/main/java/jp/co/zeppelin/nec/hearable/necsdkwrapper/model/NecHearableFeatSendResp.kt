package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

/**
 * Repr for server response to request to upload result of ear "feature" measurement via digital
 * sound playback via NEC Hearable earpiece
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class NecHearableFeatSendRespBase(
    val status: Int,
    val message: String
)

/**
 * NEC Hearable SDK server response to attempt upload ear "feature" (measured via digital sound playback)
 *  - registration complete ("message" and "deficientNum" will both be null)
 *
 * e.g.
 *  {"status":  0, "wearerId": "9ed864d4-eac1-4b2e-ad93-e35c0214dde0", "deviceId": "00:0A:45:10:DF:9A"}
 */
@JsonClass(generateAdapter = true)
class NecHearableFeatSuccessRegisterComplete(
    status: Int,
    val wearerId: String,
    val deviceId: String
) : NecHearableFeatSendRespBase(status, "") {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableFeatSuccessRegisterCompleteJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableFeatSuccessRegisterComplete? {
            return jsonAdapter.fromJson(json)
        }
    }
}

// indicates presence of "deficientNum" value, e.g. "deficientNum" of 4 means 4 more ear "feature" acquisition runs required
const val NEC_HEARABLE_STATUS_NEED_ADDITIONAL_RUNS = 2

/**
 * Repr of successful upload of ear "feature" to NEC backend servers (additional samples required
 * before registration will be complete)
 *   - "deficientNum" indicates number of additional samples required for user to complete registration;
 *     empirically will be in range [1, 5])
 *
 * e.g.
 *  {"status":  2, "wearerId": "1f9e24e6-4e8e-4e06-a0eb-45df943b95d9", "deviceId": "00:0A:45:10:DF:9A", "message": "Deficient Error", "deficientNum": 1}
 */
@JsonClass(generateAdapter = true)
class NecHearableFeatSuccessRegisterIncomplete(
    status: Int,
    val wearerId: String,
    val deviceId: String,
    message: String,
    val deficientNum: Int
) : NecHearableFeatSendRespBase(status, message) {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableFeatSuccessRegisterIncompleteJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableFeatSuccessRegisterIncomplete? {
            return jsonAdapter.fromJson(json)
        }
    }
}

/**
 * Repr of error response to attempt to upload ear "feature" (measured via digital sound playback)
 *
 * e.g.
 *  {"status": 99, "wearerId": "79476dfb-7ff9-4ec8-87fa-d87d85ccdcaf", "deviceId": "00:0A:45:10:DF:D8", "message": "Other Error"}
 */
@JsonClass(generateAdapter = true)
class NecHearableFeatSendError(
    status: Int,
    val wearerId: String,
    val deviceId: String,
    message: String
) : NecHearableFeatSendRespBase(status, message) {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableFeatSendErrorJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableFeatSendError? {
            return jsonAdapter.fromJson(json)
        }
    }
}

class NecHearableFeatSendFail(
    status: Int,
    message: String
) : NecHearableFeatSendRespBase(status, message)
