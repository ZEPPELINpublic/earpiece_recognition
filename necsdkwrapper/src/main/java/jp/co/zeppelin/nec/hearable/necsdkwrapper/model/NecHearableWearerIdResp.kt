package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

/**
 * NEC Hearable voice memo response observables: request "wearer ID" (UUID provided by NEC server)
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */

/**
 * Result of attempting to record voice memo to local .wav file
 */
sealed class NecHearableWearerIdRespBase

/**
 * Typ. requestWearerId(): status: 0, recvStr: {"deviceId": "00:0A:45:10:DF:C5", "edgeId": "a249dc489ca9533b", "wearerId": "8619e958-7663-45ea-abe1-c1a7ab0f3b58"} (NecHearableSdkWrapper.kt:161)
 */
@JsonClass(generateAdapter = true)
data class NecHearableWearerIdSuccess(
    val wearerId: String,
    val deviceId: String,
    val edgeId: String
) : NecHearableWearerIdRespBase() {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableWearerIdSuccessJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableWearerIdSuccess? {
            return jsonAdapter.fromJson(json)
        }
    }
}


data class NecHearableWearerIdFail(
    val status: Int,
    // Typ. "message" field only expected for errors
    val message: String? = null
) : NecHearableWearerIdRespBase()
