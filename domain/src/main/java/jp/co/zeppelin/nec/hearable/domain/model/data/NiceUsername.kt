package jp.co.zeppelin.nec.hearable.domain.model.data

/**
 * Pojo repr for AAC "Room" DB "NiceUsernameEntity" entity
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class NiceUsername(
    val niceUsernameId: Long,
    val niceUsername: String
)
