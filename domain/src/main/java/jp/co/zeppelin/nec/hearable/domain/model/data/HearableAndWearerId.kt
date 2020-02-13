package jp.co.zeppelin.nec.hearable.domain.model.data

/**
 * Pojo for mapping from DB entity to avoid exposing DB implementation details
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class HearableAndWearerId(
    val hearableId: Long,
    val wearerId: Long
)
