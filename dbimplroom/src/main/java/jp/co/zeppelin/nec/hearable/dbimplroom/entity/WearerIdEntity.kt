package jp.co.zeppelin.nec.hearable.dbimplroom.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AAC "Room" DB "wearer ID" entity
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Entity(
    tableName = "wearer_ids_table",
    indices = [Index(value = ["wearerIdStr"], unique = true)]
)
data class WearerIdEntity(
    @PrimaryKey(autoGenerate = true) val wearerId: Long = 0,
    val wearerIdStr: String
)
