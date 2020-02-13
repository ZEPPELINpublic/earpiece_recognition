package jp.co.zeppelin.nec.hearable.dbimplroom.db

import androidx.room.Database
import androidx.room.RoomDatabase
import jp.co.zeppelin.nec.hearable.dbimplroom.dao.*
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.*

/**
 * Version numbers
 *   1: initial version
 *   2: Add associations (wearerID and "nice" username associated with NEC Hearable ID)
 *   3: Add "is authenticated" bool to hearable table
 *   4: Add indices to junction tables
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Database(
    entities = arrayOf(
        // Nec hearable devices
        NecHearableIdEntity::class,
        // Wearer ID UUIDs
        WearerIdEntity::class,
        // "Nice" usernames
        NiceUsernameEntity::class,
        // Junction table for many:many hearables/wearerID relationships
        HearableWearerIdJctEntity::class,
        // Junction table for many:many hearables/"nice" usernames relationships
        HearableNiceUsernameJctEntity::class
    ),
    version = 4
)
abstract class HearableDb : RoomDatabase() {
    abstract fun necHearableIdDao(): NecHearableIdDao
    abstract fun wearerIdDao(): WearerIdDao
    abstract fun niceUsernameDao(): NiceUsernameDao
    abstract fun hearableWearerJctDao(): HearableWearerJctDao
    abstract fun hearableNiceUsernameJctDao(): HearableNiceUsernameJctDao
}
