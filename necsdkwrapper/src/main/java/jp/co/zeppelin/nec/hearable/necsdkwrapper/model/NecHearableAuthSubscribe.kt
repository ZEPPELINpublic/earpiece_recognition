package jp.co.zeppelin.nec.hearable.necsdkwrapper.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

/**
 * Repr for response to request to "hearableMqttRequest.requestSubscribe()"
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
sealed class NecHearableAuthSubscribeBase(
    val result: String
)

const val NEC_HEARABLE_RESULT_OK = "OK"

/**
 * For converting response from the one-two punch of e.g.
 *       hearableMqttRequest.requestSubscribe(topic, ...)
 *       hearableMqttRequest.requestAuth(param, 5)
 */
@JsonClass(generateAdapter = true)
class NecHearableAuthSubscribeSuccess(
    result: String
) : NecHearableAuthSubscribeBase(result) {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableAuthSubscribeSuccessJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableAuthSubscribeSuccess? {
            return jsonAdapter.fromJson(json)
        }
    }
}

@JsonClass(generateAdapter = true)
class NecHearableAuthSubscribeFail(
    val status: Int,
    result: String
) : NecHearableAuthSubscribeBase(result) {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableAuthSubscribeFailJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableAuthSubscribeFail? {
            return jsonAdapter.fromJson(json)
        }
    }
}
