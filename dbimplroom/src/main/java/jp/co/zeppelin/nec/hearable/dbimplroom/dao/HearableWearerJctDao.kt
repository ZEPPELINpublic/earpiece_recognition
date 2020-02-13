package jp.co.zeppelin.nec.hearable.dbimplroom.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.HearableWearerIdJctEntity

/**
 * AAC "Room" DB cross-table junction dao
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Dao
interface HearableWearerJctDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg hearableWearerPairs: HearableWearerIdJctEntity): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOne(hearableWearerPair: HearableWearerIdJctEntity): Long
}
