package jp.co.zeppelin.nec.hearable.domain.model.data

/**
 * Pojo for mapping from DB entity to avoid exposing DB implementation details
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class HearableAssoc(
    val hearableId: NecHearableId,
    val wearerIds: List<WearerId>,
    val niceUsernames: List<NiceUsername>
) {
    fun toHearableVerified(): HearableVerified = HearableVerified(
        hearableId = hearableId.hearableIdStr,
        wearerId = if (wearerIds.isNotEmpty()) wearerIds[0].wearerIdStr else "",
        niceUsername = if (niceUsernames.isNotEmpty()) niceUsernames[0].niceUsername else ""
    )
}
