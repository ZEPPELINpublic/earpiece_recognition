package jp.co.zeppelin.nec.hearable.dbimplroom.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.WearerIdEntity
import jp.co.zeppelin.nec.hearable.domain.model.data.WearerId
import kotlinx.coroutines.flow.Flow

/**
 * AAC "Room" DB "wearer ID" dao
 *
 * Bleeding-edge kotlin coroutine "flow" paradigm
 *
 * Refs:
 *      Google dev summit video https://www.youtube.com/watch?v=_aJsh6P00c0
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Dao
interface WearerIdDao {

    /**
     * Retrieve pojos instead of entities: don't leak AAC "Room" impl details
     *
     * Entity mandatory for defining DB table and insert
     */
    @Query(
        """
        SELECT * FROM wearer_ids_table
    """
    )
    fun getAllAsPojos(): Flow<List<WearerId>>

    @Query(
        """
        SELECT wearerId FROM wearer_ids_table WHERE wearerIdStr == :wearerIdStr
    """
    )
    suspend fun byId(wearerIdStr: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg wearerIds: WearerIdEntity): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOne(wearerId: WearerIdEntity): Long
}
