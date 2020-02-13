package jp.co.zeppelin.nec.hearable.dbimplroom.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.HearableNiceUsernameJctEntity

/**
 * AAC "Room" DB cross-table junction dao
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Dao
interface HearableNiceUsernameJctDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg users: HearableNiceUsernameJctEntity): List<Long>
}
