package jp.co.zeppelin.nec.hearable.data.impl

import android.content.Context
import jp.co.zeppelin.nec.hearable.data.contract.IDataRepo
import jp.co.zeppelin.nec.hearable.dbimplroom.contract.IDBImplHelper
import jp.co.zeppelin.nec.hearable.dbimplroom.db.DBImplHelper
import jp.co.zeppelin.nec.hearable.domain.model.data.HearableAssoc
import jp.co.zeppelin.nec.hearable.domain.model.data.NecHearableId
import jp.co.zeppelin.nec.hearable.domain.model.data.NiceUsername
import jp.co.zeppelin.nec.hearable.domain.model.data.WearerId
import kotlinx.coroutines.flow.Flow

/**
 * "Data" repository-pattern module top level impl
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
class DataRepoImpl(context: Context, isTest: Boolean = false) :
    IDataRepo.IDataRepoImpl {

    private val aacRoomDbHelper: IDBImplHelper = DBImplHelper(context, isTest)

    override fun getHearableDatasetFor(hearableIdStr: String) =
        aacRoomDbHelper.getHearableDatasetFor(hearableIdStr)

    override fun updateAuthenticateHearableIdFor(hearableIdStr: String) =
        aacRoomDbHelper.updateAuthenticateHearableIdFor(hearableIdStr)

    override suspend fun insertAndGetHearableIdFor(hearableIdStr: String) =
        aacRoomDbHelper.insertAndGetHearableIdFor(hearableIdStr)

    override suspend fun insertAngGetWearerIdFor(wearerIdStr: String) =
        aacRoomDbHelper.insertAngGetWearerIdFor(wearerIdStr)

    override suspend fun insertAndGetNiceUsernameIdFor(niceUsername: String) =
        aacRoomDbHelper.insertAndGetNiceUsernameIdFor(niceUsername)

    override fun getAllHearableIds(): Flow<List<NecHearableId>> =
        aacRoomDbHelper.getAllHearableIds()

    override fun getAllWearerIds(): Flow<List<WearerId>> = aacRoomDbHelper.getAllWearerIds()
    override fun getAllNiceUsernames(): Flow<List<NiceUsername>> =
        aacRoomDbHelper.getAllNiceUsernames()

    override suspend fun insertHearableWearerIdPair(hearableIdStr: String, wearerIdStr: String) =
        aacRoomDbHelper.insertHearableWearerIdPair(hearableIdStr, wearerIdStr)

    override suspend fun insertHearableUsernamePair(hearableIdStr: String, niceUsername: String) =
        aacRoomDbHelper.insertHearableUsernamePair(hearableIdStr, niceUsername)

    override fun getAllHearableDatasets(): Flow<List<HearableAssoc>> =
        aacRoomDbHelper.getAllHearableDatasets()
}
