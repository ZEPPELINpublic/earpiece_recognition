package jp.co.zeppelin.nec.hearable.dbimplroom.db

import android.content.Context
import androidx.room.Room
import jp.co.zeppelin.nec.hearable.dbimplroom.contract.IDBImplHelper
import jp.co.zeppelin.nec.hearable.dbimplroom.entity.*
import jp.co.zeppelin.nec.hearable.domain.helpers.DeviceId
import jp.co.zeppelin.nec.hearable.domain.model.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class DBImplHelper(context: Context, isTest: Boolean = false) :
    IDBImplHelper {
    private val TAG = DBImplHelper::class.java.simpleName

    private val roomDBBuilder = if (isTest) {
        Room.inMemoryDatabaseBuilder(context, HearableDb::class.java)
    } else {
        Room.databaseBuilder(
            context,
            HearableDb::class.java, "nec-hearable-db-room"
        )
    }
    private val roomDB = roomDBBuilder
        .fallbackToDestructiveMigration()
        .build()

    override fun getHearableDatasetFor(hearableIdStr: String): Flow<HearableAssocOneToOne?> {
        val deviceIdStr = DeviceId.deviceIdFromMacAddress(hearableIdStr)
        return flow {
            roomDB.necHearableIdDao().allDataFor(deviceIdStr).collect { entity ->
                emit(HearableAssocOneToOne.factory(entity?.toPojo()))
            }
        }
    }

    override fun getAllHearableIds(): Flow<List<NecHearableId>> =
        roomDB.necHearableIdDao().getAllAsPojos()

    override fun updateAuthenticateHearableIdFor(hearableIdStr: String) = flow {
        val deviceIdStr = DeviceId.deviceIdFromMacAddress(hearableIdStr)
        roomDB.necHearableIdDao().hearableFor(deviceIdStr).collect {
            val updatedEntity = NecHearableIdEntity(
                it.hearableId,
                it.hearableIdStr,
                true
            )
            emit(roomDB.necHearableIdDao().updateHearableID(updatedEntity))
        }
    }

    override suspend fun insertAndGetHearableIdFor(hearableIdStr: String): Long {
        val deviceIdStr = DeviceId.deviceIdFromMacAddress(hearableIdStr)
        val myEntity = NecHearableIdEntity(hearableIdStr = deviceIdStr)
        val id = roomDB.necHearableIdDao().insertOne(myEntity)
        return if (id > 0) {
            id
        } else {
            roomDB.necHearableIdDao().byId(deviceIdStr)
        }
    }

    override fun getAllWearerIds(): Flow<List<WearerId>> = roomDB.wearerIdDao().getAllAsPojos()

    override suspend fun insertAngGetWearerIdFor(wearerIdStr: String): Long {
        val id = roomDB.wearerIdDao().insertOne(WearerIdEntity(wearerIdStr = wearerIdStr))
        return if (id > 0) {
            id
        } else {
            roomDB.wearerIdDao().byId(wearerIdStr)
        }
    }

    override fun getAllNiceUsernames(): Flow<List<NiceUsername>> =
        roomDB.niceUsernameDao().getAllAsPojos()

    override suspend fun insertAndGetNiceUsernameIdFor(niceUsername: String): Long {
        val id = roomDB.niceUsernameDao().insertOne(NiceUsernameEntity(niceUsername = niceUsername))
        return if (id > 0) {
            id
        } else {
            roomDB.niceUsernameDao().byId(niceUsername)
        }
    }

    override suspend fun insertHearableWearerIdPair(
        hearableIdStr: String,
        wearerIdStr: String
    ): HearableAndWearerId {
        val hearableId = insertAndGetHearableIdFor(hearableIdStr)
        val wearerId = insertAngGetWearerIdFor(wearerIdStr)
        roomDB.hearableWearerJctDao()
            .insertAll(HearableWearerIdJctEntity(hearableId, wearerId))
        return HearableAndWearerId(hearableId, wearerId)
    }

    override suspend fun insertHearableUsernamePair(
        hearableIdStr: String,
        niceUsername: String
    ): HearableAndNiceUsernameId {
        val hearableId = insertAndGetHearableIdFor(hearableIdStr)
        val niceUsernameId = insertAndGetNiceUsernameIdFor(niceUsername)
        roomDB.hearableNiceUsernameJctDao()
            .insertAll(HearableNiceUsernameJctEntity(hearableId, niceUsernameId))
        return HearableAndNiceUsernameId(hearableId, niceUsernameId)
    }

    override fun getAllHearableDatasets(): Flow<List<HearableAssoc>> {
        return flow {
            roomDB.necHearableIdDao().allData().collect { entities ->
                emit(entities.map { entity -> entity.toPojo() })
            }
        }
    }
}
