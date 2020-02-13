package jp.co.zeppelin.nec.hearable.dbimplroom.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * AAC "Room" DB Hearable ID entity
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Entity(
    tableName = "nec_hearables_table",
    indices = [Index(value = ["hearableIdStr"], unique = true)]
)
data class NecHearableIdEntity(
    @PrimaryKey(autoGenerate = true) val hearableId: Long = 0,
    val hearableIdStr: String,
    // Has user completed entire digital sound playback procedure and considered authenticated by
    // NEC backend/server?  If not, don't want to show device in list on "Log in" screen
    val isAuthenticated: Boolean = false
)
