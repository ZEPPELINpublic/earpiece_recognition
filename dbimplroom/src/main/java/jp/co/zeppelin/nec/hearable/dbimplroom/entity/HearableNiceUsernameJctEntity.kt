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
    tableName = "hearable_nice_username_jct_table",
    primaryKeys = ["hearableId", "niceUsernameId"],
    indices = [Index(value = ["niceUsernameId"])]
)
data class HearableNiceUsernameJctEntity(
    val hearableId: Long,
    val niceUsernameId: Long
)
