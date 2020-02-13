package jp.co.zeppelin.nec.hearable.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.zeppelin.nec.hearable.data.contract.IDataRepo
import jp.co.zeppelin.nec.hearable.data.impl.DataRepoImpl
import jp.co.zeppelin.nec.hearable.domain.helpers.ZepLog
import jp.co.zeppelin.nec.hearable.domain.model.data.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test AAC "Room" DB impl indirecting via top level repository-pattern "data" module abstraction
 */
@RunWith(AndroidJUnit4::class)
@kotlinx.coroutines.InternalCoroutinesApi
class DataRepoDbModernTests {
    private val TAG = DataRepoDbModernTests::class.java.simpleName
    // Context of the app under test.
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    var idPriKeyHearable = -1L
    var idPriKeyWearerId = -1L
    var idPriKeyNiceUsername = -1L
    var listHearables: List<NecHearableId>? = null
    var listWearerIds: List<WearerId>? = null
    var listNiceUsernames: List<NiceUsername>? = null
    var listAssoc: List<HearableAssoc>? = null
    var assocA: HearableAssocOneToOne? = null

    @Before
    fun setup() {
        idPriKeyHearable = -1L
        idPriKeyWearerId = -1L
        idPriKeyNiceUsername = -1L
        listHearables = null
        listWearerIds = null
        listNiceUsernames = null
    }

    fun insertHearableIdModern(
        dataRepo: IDataRepo.IDataRepoImpl,
        hearableMacAddr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            idPriKeyHearable = dataRepo.insertAndGetHearableIdFor(hearableMacAddr)
            actionDone()
        }
    }

    fun getAllHearables(dataRepo: IDataRepo.IDataRepoImpl, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dataRepo.getAllHearableIds()
            idPriKeyFlow.collect { allHearables ->
                listHearables = allHearables
                actionDone()
            }
        }
    }

    fun insertWearerIdModern(
        dataRepo: IDataRepo.IDataRepoImpl,
        wearerIdStr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            idPriKeyWearerId = dataRepo.insertAngGetWearerIdFor(wearerIdStr)
            actionDone()
        }
    }

    fun getAllWearerIds(dataRepo: IDataRepo.IDataRepoImpl, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dataRepo.getAllWearerIds()
            idPriKeyFlow.collect { allWearerIds ->
                listWearerIds = allWearerIds
                actionDone()
            }
        }
    }

    fun insertNiceUsernameModern(
        dataRepo: IDataRepo.IDataRepoImpl,
        niceUsername: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            idPriKeyNiceUsername = dataRepo.insertAndGetNiceUsernameIdFor(niceUsername)
            actionDone()
        }
    }

    fun getAllNiceUsernames(dataRepo: IDataRepo.IDataRepoImpl, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dataRepo.getAllNiceUsernames()
            idPriKeyFlow.collect { niceUsernames ->
                listNiceUsernames = niceUsernames
                actionDone()
            }
        }
    }

    fun insertHearableWearerIdPairJob(
        dataRepo: IDataRepo.IDataRepoImpl,
        hearableIdStr: String,
        wearerIdStr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            dataRepo.insertHearableWearerIdPair(hearableIdStr, wearerIdStr)
            actionDone()
        }
    }

    fun insertHearableNiceUsernamePairJob(
        dataRepo: IDataRepo.IDataRepoImpl,
        hearableIdStr: String,
        niceUsername: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            dataRepo.insertHearableUsernamePair(hearableIdStr, niceUsername)
            actionDone()
        }
    }

    fun getAllAssocJob(dataRepo: IDataRepo.IDataRepoImpl, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dataRepo.getAllHearableDatasets()
            idPriKeyFlow.collect { allAssoc ->
                listAssoc = allAssoc
                actionDone()
            }
        }
    }

    fun getDatasetFor(
        dataRepo: IDataRepo.IDataRepoImpl,
        hearableMacAddr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dataRepo.getHearableDatasetFor(hearableMacAddr)
            idPriKeyFlow.collect { assoc ->
                assocA = assoc
                actionDone()
            }
        }
    }

    fun assertDatasetFor(hearableMacAddr: String) {
        ZepLog.d(TAG, "assertDatasetFor(): (assert for $hearableMacAddr): assocA: $assocA")
        assertNotNull(assocA)
        val hearableId = when (hearableMacAddr) {
            DbTestHelpers.hearableAMacAddr -> DbTestHelpers.hearableAId
            DbTestHelpers.hearableBMacAddr -> DbTestHelpers.hearableBId
            else -> DbTestHelpers.hearableCId
        }
        val wearerIdStr = when (hearableMacAddr) {
            DbTestHelpers.hearableAMacAddr -> DbTestHelpers.hearableAWearerId
            DbTestHelpers.hearableBMacAddr -> DbTestHelpers.hearableBWearerId
            else -> DbTestHelpers.hearableCWearerId
        }
        val niceUsername = when (hearableMacAddr) {
            DbTestHelpers.hearableAMacAddr -> DbTestHelpers.hearableANiceUsername
            DbTestHelpers.hearableBMacAddr -> DbTestHelpers.hearableBNiceUsername
            else -> DbTestHelpers.hearableCNiceUsername
        }
        assertEquals(hearableId, assocA?.hearableId)
        assertEquals(wearerIdStr, assocA?.wearerId)
        assertEquals(niceUsername, assocA?.niceUsername)
    }


    @Test
    fun populateThreeFullSets() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPassA_1of2 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassA_2of2 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "populateThreeFullSets(): pass 1: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_1of2 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_2of2 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBNiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobBGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobBGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(2, listAssoc?.size)
        ZepLog.d(TAG, "populateThreeFullSets(): pass 2: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobPassC_1of2 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableCMacAddr,
            DbTestHelpers.hearableCWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassC_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassC_2of2 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableCMacAddr,
            DbTestHelpers.hearableCNiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassC_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobCGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobCGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(3, listAssoc?.size)
        ZepLog.d(TAG, "populateThreeFullSets(): pass 3: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStr = getDatasetFor(
            dataRepo,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStr) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableAMacAddr)

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStrB = getDatasetFor(
            dataRepo,
            DbTestHelpers.hearableBMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStrB) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableBMacAddr)

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStrC = getDatasetFor(
            dataRepo,
            DbTestHelpers.hearableCMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStrC) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableCMacAddr)
    }

    @Test
    fun populateTwoFullSets() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPassA_1of2 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassA_2of2 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "populateTwoFullSets(): pass 1: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_1of2 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_2of2 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBNiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobBGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobBGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(2, listAssoc?.size)
        ZepLog.d(TAG, "populateTwoFullSets(): pass 2: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStr = getDatasetFor(
            dataRepo,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStr) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableAMacAddr)

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStrB = getDatasetFor(
            dataRepo,
            DbTestHelpers.hearableBMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStrB) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableBMacAddr)
    }

    @Test
    fun populateOneFullSet() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPass1of3 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPass2of3 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "populateOneFullSet(): $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStr = getDatasetFor(
            dataRepo,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStr) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableAMacAddr)
    }

    @Test
    fun insertHearableNiceUsernamePairTogether() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPass1of3 = insertHearableNiceUsernamePairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dataRepo) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "insertHearableNiceUsernamePairTogether(): $listAssoc")
    }

    @Test
    fun insertHearableWearerIdPairTogether() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        var isHearableWearerIdCollectFinished = false
        val jobPass1of3 = insertHearableWearerIdPairJob(
            dataRepo,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableWearerIdCollectFinished }

        isHearableWearerIdCollectFinished = false
        val jobGetAll = getAllAssocJob(dataRepo) { isHearableWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableWearerIdCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "insertHearableWearerIdPairTogether(): $listAssoc")
    }

    /**
     * Attempt to insert same NEC Hearable ID more than once will be ignored, but the correct ID will
     * be returned.
     */
    @Test
    fun niceUsernameInsert3TimesAndVerifyUnique() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        // Pass 1 of 3
        var isNiceUsernameCollectFinished = false
        val jobPass1of3 = insertNiceUsernameModern(
            dataRepo,
            DbTestHelpers.hearableANiceUsername
        ) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isNiceUsernameCollectFinished }
        val idPriKeyCaptured = idPriKeyNiceUsername
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyNiceUsername)

        // Pass 2 of 3
        isNiceUsernameCollectFinished = false
        val jobPass2of3 = insertNiceUsernameModern(
            dataRepo,
            DbTestHelpers.hearableANiceUsername
        ) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isNiceUsernameCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyNiceUsername)

        // Pass 3 of 3
        isNiceUsernameCollectFinished = false
        val jobPass3of3 = insertNiceUsernameModern(
            dataRepo,
            DbTestHelpers.hearableANiceUsername
        ) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass3of3) { isNiceUsernameCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyNiceUsername)

        isNiceUsernameCollectFinished = false
        val jobGetAll = getAllNiceUsernames(dataRepo) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isNiceUsernameCollectFinished }
        assertEquals(1, listNiceUsernames?.size)
        ZepLog.d(TAG, "niceUsernameInsert3TimesAndVerifyUnique(): $listNiceUsernames")
    }

    /**
     * Attempt to insert same NEC Hearable ID more than once will be ignored, but the correct ID will
     * be returned.
     */
    @Test
    fun wearerIdInsert3TimesAndVerifyUnique() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        // Pass 1 of 3
        var isWearerIdCollectFinished = false
        val jobPass1of3 = insertWearerIdModern(
            dataRepo,
            DbTestHelpers.hearableAWearerId
        ) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isWearerIdCollectFinished }
        val idPriKeyCaptured = idPriKeyWearerId
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyWearerId)

        // Pass 2 of 3
        isWearerIdCollectFinished = false
        val jobPass2of3 = insertWearerIdModern(
            dataRepo,
            DbTestHelpers.hearableAWearerId
        ) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isWearerIdCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyWearerId)

        // Pass 3 of 3
        isWearerIdCollectFinished = false
        val jobPass3of3 = insertWearerIdModern(
            dataRepo,
            DbTestHelpers.hearableAWearerId
        ) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass3of3) { isWearerIdCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyWearerId)

        isWearerIdCollectFinished = false
        val jobGetAll = getAllWearerIds(dataRepo) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isWearerIdCollectFinished }
        assertEquals(1, listWearerIds?.size)
        ZepLog.d(TAG, "wearerIdInsert3TimesAndVerifyUnique(): $listWearerIds")
    }

    /**
     * Attempt to insert same NEC Hearable ID more than once will be ignored, but the correct ID will
     * be returned.
     */
    @Test
    fun hearableIdInsert3TimesAndVerifyUnique() {
        val dataRepo: IDataRepo.IDataRepoImpl = DataRepoImpl(appContext, isTest = true)

        // Pass 1 of 3
        var isHearableCollectFinished = false
        val jobPass1of3 = insertHearableIdModern(
            dataRepo,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableCollectFinished }
        val idPriKeyCaptured = idPriKeyHearable
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        // Pass 2 of 3
        isHearableCollectFinished = false
        val jobPass2of3 = insertHearableIdModern(
            dataRepo,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isHearableCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        // Pass 3 of 3
        isHearableCollectFinished = false
        val jobPass3of3 = insertHearableIdModern(
            dataRepo,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass3of3) { isHearableCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        isHearableCollectFinished = false
        val jobGetAll = getAllHearables(dataRepo) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableCollectFinished }
        assertEquals(1, listHearables?.size)
        ZepLog.d(TAG, "hearableIdInsert3TimesAndVerifyUnique(): $listHearables")
    }
}
