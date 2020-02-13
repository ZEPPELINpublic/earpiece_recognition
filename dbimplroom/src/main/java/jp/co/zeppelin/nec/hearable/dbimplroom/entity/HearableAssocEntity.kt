package jp.co.zeppelin.nec.hearable.dbimplroom.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import jp.co.zeppelin.nec.hearable.domain.model.data.HearableAssoc
import jp.co.zeppelin.nec.hearable.domain.model.data.NecHearableId
import jp.co.zeppelin.nec.hearable.domain.model.data.NiceUsername
import jp.co.zeppelin.nec.hearable.domain.model.data.WearerId

/**
 * AAC "Room" DB cross-table association entity
 *
 * Avoids need for explicit FKs (Foreign Keys)
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
data class HearableAssocEntity(
    @Embedded val hearableId: NecHearableId,
    @Relation(
        parentColumn = "hearableId",
        entity = WearerIdEntity::class,
        entityColumn = "wearerId",
        associateBy = Junction(HearableWearerIdJctEntity::class)
    )
    val wearerIds: List<WearerId>,
    @Relation(
        parentColumn = "hearableId",
        entity = NiceUsernameEntity::class,
        entityColumn = "niceUsernameId",
        associateBy = Junction(HearableNiceUsernameJctEntity::class)
    )
    val niceUsernames: List<NiceUsername>
) {
    fun toPojo(): HearableAssoc {
        return HearableAssoc(
            hearableId = hearableId,
            wearerIds = wearerIds,
            niceUsernames = niceUsernames
        )
    }
}
