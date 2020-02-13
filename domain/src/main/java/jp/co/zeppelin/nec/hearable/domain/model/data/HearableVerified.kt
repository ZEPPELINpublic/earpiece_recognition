package jp.co.zeppelin.nec.hearable.domain.model.data

/**
 * Result of bluetooth device discovery (scan) which also includes whether the device
 * is both bluetooth classic paired (bonded) and connected
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class HearableVerified(
    val hearableId: String,
    val wearerId: String,
    val niceUsername: String
)
