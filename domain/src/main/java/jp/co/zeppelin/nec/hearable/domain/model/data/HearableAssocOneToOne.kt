package jp.co.zeppelin.nec.hearable.domain.model.data

/**
 * Pojo for mapping from DB entity to avoid exposing DB implementation details
 *
 * Note: AAC "Room" DB is a private implementation detail; pass data using pojos not DB entities
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class HearableAssocOneToOne(
    val hearableId: String,
    val wearerId: String,
    val niceUsername: String
) {
    companion object {
        fun factory(pojo: HearableAssoc?): HearableAssocOneToOne {
            var wearerId = ""
            var niceUsername = ""
            pojo?.apply {
                if (wearerIds.isNotEmpty()) {
                    wearerId = wearerIds[0].wearerIdStr
                }
                if (niceUsernames.isNotEmpty()) {
                    niceUsername = niceUsernames[0].niceUsername
                }
            }
            return HearableAssocOneToOne(
                hearableId = pojo?.hearableId?.hearableIdStr ?: "",
                wearerId = wearerId,
                niceUsername = niceUsername
            )
        }
    }
}
