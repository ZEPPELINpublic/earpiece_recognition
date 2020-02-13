package jp.co.zeppelin.nec.hearable.domain.model.data

/**
 * Pojo repr for AAC "Room" DB "WearerIdEntity" entity
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class WearerId(
    val wearerId: Long,
    val wearerIdStr: String
)
