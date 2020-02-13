package jp.co.zeppelin.nec.hearable.domain.model.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson

/**
 * Pojo repr for AAC "Room" DB "NecHearableIdEntity" entity
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@JsonClass(generateAdapter = true)
data class NecHearableId(
    val hearableId: Long,
    val hearableIdStr: String,
    val isAuthenticated: Boolean
) {
    @ToJson
    fun toJson(): String {
        return jsonAdapter.toJson(this)
    }

    companion object {
        val moshi = Moshi.Builder().build()
        val jsonAdapter = NecHearableIdJsonAdapter(moshi)

        @FromJson
        fun fromJson(json: String): NecHearableId? {
            return jsonAdapter.fromJson(json)
        }
    }
}
