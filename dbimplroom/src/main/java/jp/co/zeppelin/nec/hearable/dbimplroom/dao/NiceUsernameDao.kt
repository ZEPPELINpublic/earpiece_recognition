package jp.co.zeppelin.nec.hearable.dbimplroom.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.NiceUsernameEntity
import jp.co.zeppelin.nec.hearable.domain.model.data.NiceUsername
import kotlinx.coroutines.flow.Flow

/**
 * AAC "Room" DB "nice" username dao
 *
 * Bleeding-edge kotlin coroutine "flow" paradigm
 *
 * Refs:
 *      Google dev summit video https://www.youtube.com/watch?v=_aJsh6P00c0
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Dao
interface NiceUsernameDao {

    /**
     * Retrieve pojos instead of entities: don't leak AAC "Room" impl details
     *
     * Entity mandatory for defining DB table and insert
     */
    @Query(
        """
        SELECT * FROM nice_usernames_table
    """
    )
    fun getAllAsPojos(): Flow<List<NiceUsername>>

    @Query(
        """
        SELECT niceUsernameId FROM nice_usernames_table WHERE niceUsername == :niceUsername
    """
    )
    suspend fun byId(niceUsername: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg niceUsernames: NiceUsernameEntity): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOne(niceUsername: NiceUsernameEntity): Long
}
