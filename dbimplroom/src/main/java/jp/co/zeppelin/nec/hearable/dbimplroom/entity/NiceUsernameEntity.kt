package jp.co.zeppelin.nec.hearable.dbimplroom.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AAC "Room" DB "nice" username entity
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Entity(
    tableName = "nice_usernames_table",
    indices = [Index(value = ["niceUsername"], unique = true)]
)
data class NiceUsernameEntity(
    @PrimaryKey(autoGenerate = true) val niceUsernameId: Long = 0,
    val niceUsername: String
)
