package jp.co.zeppelin.nec.hearable.dbimplroom.entity

import androidx.room.Entity
import androidx.room.Index

/**
 * AAC "Room" DB cross-table junction entity
 *
 * Avoids need for explicit FKs (Foreign Keys)
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Entity(
    tableName = "hearable_wearer_jct_table",
    primaryKeys = ["hearableId", "wearerId"],
    indices = [Index(value = ["wearerId"])]
)
data class HearableWearerIdJctEntity(
    val hearableId: Long,
    val wearerId: Long
)
