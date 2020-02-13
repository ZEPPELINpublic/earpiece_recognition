package jp.co.zeppelin.nec.hearable.data.contract

import androidx.lifecycle.LiveData
import jp.co.zeppelin.nec.hearable.domain.helpers.SingleLiveDataEvent
import jp.co.zeppelin.nec.hearable.domain.model.data.*
import kotlinx.coroutines.flow.Flow

/**
 * Top-level contract I/F for "data" repository-pattern module
 *
 * Note: don't use kotlin coroutine (cold) "Flow" type for inserts; w/o collector means they never happen
 *
 * (c) 2020 Zeppelin Inc., Shibuya, Tokyo JAPAN
 */
interface IDataRepo {

    interface IDataRepoImpl : IDataRepo {
        /**
         * Get object triplet for NEC Hearable ID
         *  - NEC Hearable ID
         *  - wearer ID
         *  - "nice" user name
         *
         *  Typ. will be displayed in a list so returning users can select their name and get running quickly
         */
        fun getHearableDatasetFor(hearableIdStr: String): Flow<HearableAssocOneToOne?>

        fun updateAuthenticateHearableIdFor(hearableIdStr: String): Flow<Int>

        /**
         * Insert pair of
         *  - NEC Hearable ID (MAC address)
         *  - wearerID (UUID from server)
         *
         *  Will insert (or use already inserted values for each) and associate wearerID with NEC Hearable ID
         */
        suspend fun insertHearableWearerIdPair(
            hearableIdStr: String,
            wearerIdStr: String
        ): HearableAndWearerId

        /**
         * Insert pair of
         *  - NEC Hearable ID (MAC address)
         *  - "nice" username (as provided by user)
         *
         *  Will insert (or use already inserted values for each) and associate "nice" username with NEC Hearable ID
         */
        suspend fun insertHearableUsernamePair(
            hearableIdStr: String,
            niceUsername: String
        ): HearableAndNiceUsernameId

        suspend fun insertAndGetHearableIdFor(hearableIdStr: String): Long
        suspend fun insertAngGetWearerIdFor(wearerIdStr: String): Long
        suspend fun insertAndGetNiceUsernameIdFor(niceUsername: String): Long

        fun getAllHearableIds(): Flow<List<NecHearableId>>
        fun getAllWearerIds(): Flow<List<WearerId>>
        fun getAllNiceUsernames(): Flow<List<NiceUsername>>

        /**
         * Get all object triplets
         *  - NEC Hearable ID
         *  - wearer ID
         *  - "nice" user name
         *
         *  Typ. will be displayed in a list so returning users can select their name and get running quickly
         */
        fun getAllHearableDatasets(): Flow<List<HearableAssoc>>
    }

    interface IDataRepoUI : IDataRepo {
        /**
         * Observable which triggers insert set of
         *  - NEC Hearable ID (MAC address)
         *  - wearerID (UUID from server)
         *  - "nice" username (as provided by user)
         *
         *  Precondition: VM local vars for the above must be up-to-date
         */
        val hearableDatasetInsertedInDB: LiveData<HearableWearerUsernameIds>
        val registeredHearableDatasetsFromDB: LiveData<List<HearableAssoc>>
        val hearableDatasetFromDBForHearableID: LiveData<SingleLiveDataEvent<HearableAssocOneToOne?>>
        val hearableRegistered: LiveData<Int>

        fun checkNiceUsernameAlreadyInUse(niceUsername: String, action: (Boolean) -> Unit)
    }
}
