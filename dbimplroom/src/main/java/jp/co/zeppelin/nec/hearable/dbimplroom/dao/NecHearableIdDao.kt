package jp.co.zeppelin.nec.hearable.dbimplroom.dao

import androidx.room.*
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.HearableAssocEntity
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.NecHearableIdEntity
import jp.co.zeppelin.nec.hearable.domain.model.data.NecHearableId
import kotlinx.coroutines.flow.Flow

/**
 * AAC "Room" DB Hearable ID dao
 *
 * Bleeding-edge kotlin coroutine "flow" paradigm
 *
 * Refs:
 *      Google dev summit video https://www.youtube.com/watch?v=_aJsh6P00c0
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
@Dao
interface NecHearableIdDao {

    @Transaction
    @Query(
        """
        SELECT * FROM nec_hearables_table
    """
    )
    fun allData(): Flow<List<HearableAssocEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM nec_hearables_table WHERE hearableIdStr == :hearableIdStr
    """
    )
    fun allDataFor(hearableIdStr: String): Flow<HearableAssocEntity?>

    /**
     * Retrieve pojos instead of entities: don't leak AAC "Room" impl details
     *
     * Entity mandatory for defining DB table and insert
     */
    @Query(
        """
        SELECT * FROM nec_hearables_table
    """
    )
    fun getAllAsPojos(): Flow<List<NecHearableId>>

    @Transaction
    @Query(
        """
        SELECT * FROM nec_hearables_table WHERE hearableIdStr == :hearableIdStr
    """
    )
    fun hearableFor(hearableIdStr: String): Flow<NecHearableIdEntity>

    /**
     * Use to mark "is authenticated"
     *
     * @return  number of rows updated
     */
    @Update
    fun updateHearableID(hearableId: NecHearableIdEntity): Int

    @Query(
        """
        SELECT hearableId FROM nec_hearables_table WHERE hearableIdStr == :hearableIdStr
    """
    )
    suspend fun byId(hearableIdStr: String): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg hearableIds: NecHearableIdEntity): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOne(hearableId: NecHearableIdEntity): Long
}
