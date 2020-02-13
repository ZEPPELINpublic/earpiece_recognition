package jp.co.zeppelin.nec.hearable.dbimplroom

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.zeppelin.nec.hearable.dbimplroom.contract.IDBImplHelper
import jp.co.zeppelin.nec.hearable.dbimplroom.db.DBImplHelper
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
 * Tests for contract I/F intended to make module more convenient for consumers to use, e.g.
 * as consumer of this module I don't ever want to have to care about primary key IDs; all I
 * have available are NEC Hearable IDs (MAC addresses) and so I want to transparently "do stuff"
 * with those values instead of having to get into raw DB queries myself
 */
@RunWith(AndroidJUnit4::class)
@kotlinx.coroutines.InternalCoroutinesApi
class AacRoomDbModernTests {
    private val TAG = AacRoomDbModernTests::class.java.simpleName

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
        dbHelper: IDBImplHelper,
        hearableMacAddr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            idPriKeyHearable = dbHelper.insertAndGetHearableIdFor(hearableMacAddr)
            actionDone()
        }
    }

    fun getAllHearables(dbHelper: IDBImplHelper, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dbHelper.getAllHearableIds()
            idPriKeyFlow.collect { allHearables ->
                listHearables = allHearables
                actionDone()
            }
        }
    }

    fun insertWearerIdModern(
        dbHelper: IDBImplHelper,
        wearerIdStr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            idPriKeyWearerId = dbHelper.insertAngGetWearerIdFor(wearerIdStr)
            actionDone()
        }
    }

    fun getAllWearerIds(dbHelper: IDBImplHelper, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dbHelper.getAllWearerIds()
            idPriKeyFlow.collect { allWearerIds ->
                listWearerIds = allWearerIds
                actionDone()
            }
        }
    }

    fun insertNiceUsernameModern(
        dbHelper: IDBImplHelper,
        niceUsername: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            idPriKeyNiceUsername = dbHelper.insertAndGetNiceUsernameIdFor(niceUsername)
            actionDone()
        }
    }

    fun getAllNiceUsernames(dbHelper: IDBImplHelper, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dbHelper.getAllNiceUsernames()
            idPriKeyFlow.collect { niceUsernames ->
                listNiceUsernames = niceUsernames
                actionDone()
            }
        }
    }

    fun insertHearableWearerIdPairJob(
        dbHelper: IDBImplHelper,
        hearableIdStr: String,
        wearerIdStr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            dbHelper.insertHearableWearerIdPair(hearableIdStr, wearerIdStr)
            actionDone()
        }
    }

    fun insertHearableNiceUsernamePairJob(
        dbHelper: IDBImplHelper,
        hearableIdStr: String,
        niceUsername: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            dbHelper.insertHearableUsernamePair(hearableIdStr, niceUsername)
            actionDone()
        }
    }

    fun getAllAssocJob(dbHelper: IDBImplHelper, actionDone: () -> Unit): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dbHelper.getAllHearableDatasets()
            idPriKeyFlow.collect { allAssoc ->
                listAssoc = allAssoc
                actionDone()
            }
        }
    }

    fun getDatasetFor(
        dbHelper: IDBImplHelper,
        hearableMacAddr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dbHelper.getHearableDatasetFor(hearableMacAddr)
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
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPassA_1of2 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassA_2of2 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "populateThreeFullSets(): pass 1: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_1of2 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_2of2 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBNiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobBGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobBGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(2, listAssoc?.size)
        ZepLog.d(TAG, "populateThreeFullSets(): pass 2: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobPassC_1of2 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableCMacAddr,
            DbTestHelpers.hearableCWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassC_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassC_2of2 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableCMacAddr,
            DbTestHelpers.hearableCNiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassC_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobCGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobCGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(3, listAssoc?.size)
        ZepLog.d(TAG, "populateThreeFullSets(): pass 3: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStr = getDatasetFor(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStr) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableAMacAddr)

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStrB = getDatasetFor(
            dbHelper,
            DbTestHelpers.hearableBMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStrB) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableBMacAddr)

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStrC = getDatasetFor(
            dbHelper,
            DbTestHelpers.hearableCMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStrC) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableCMacAddr)
    }

    @Test
    fun populateTwoFullSets() {
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPassA_1of2 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassA_2of2 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassA_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "populateTwoFullSets(): pass 1: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_1of2 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_1of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPassB_2of2 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableBMacAddr,
            DbTestHelpers.hearableBNiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPassB_2of2) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobBGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobBGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(2, listAssoc?.size)
        ZepLog.d(TAG, "populateTwoFullSets(): pass 2: $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStr = getDatasetFor(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStr) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableAMacAddr)

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStrB = getDatasetFor(
            dbHelper,
            DbTestHelpers.hearableBMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStrB) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableBMacAddr)
    }

    @Test
    fun populateOneFullSet() {
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPass1of3 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobPass2of3 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "populateOneFullSet(): $listAssoc")

        isHearableNiceUsernameCollectFinished = false
        val jobGetForHearableIdStr = getDatasetFor(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetForHearableIdStr) { isHearableNiceUsernameCollectFinished }
        assertDatasetFor(DbTestHelpers.hearableAMacAddr)
    }

    @Test
    fun insertHearableNiceUsernamePairTogether() {
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        var isHearableNiceUsernameCollectFinished = false
        val jobPass1of3 = insertHearableNiceUsernamePairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableANiceUsername
        ) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableNiceUsernameCollectFinished }

        isHearableNiceUsernameCollectFinished = false
        val jobGetAll = getAllAssocJob(dbHelper) { isHearableNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableNiceUsernameCollectFinished }
        assertEquals(1, listAssoc?.size)
        ZepLog.d(TAG, "insertHearableNiceUsernamePairTogether(): $listAssoc")
    }

    @Test
    fun insertHearableWearerIdPairTogether() {
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        var isHearableWearerIdCollectFinished = false
        val jobPass1of3 = insertHearableWearerIdPairJob(
            dbHelper,
            DbTestHelpers.hearableAMacAddr,
            DbTestHelpers.hearableAWearerId
        ) { isHearableWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableWearerIdCollectFinished }

        isHearableWearerIdCollectFinished = false
        val jobGetAll = getAllAssocJob(dbHelper) { isHearableWearerIdCollectFinished = true }
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
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        // Pass 1 of 3
        var isNiceUsernameCollectFinished = false
        val jobPass1of3 = insertNiceUsernameModern(
            dbHelper,
            DbTestHelpers.hearableANiceUsername
        ) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isNiceUsernameCollectFinished }
        val idPriKeyCaptured = idPriKeyNiceUsername
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyNiceUsername)

        // Pass 2 of 3
        isNiceUsernameCollectFinished = false
        val jobPass2of3 = insertNiceUsernameModern(
            dbHelper,
            DbTestHelpers.hearableANiceUsername
        ) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isNiceUsernameCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyNiceUsername)

        // Pass 3 of 3
        isNiceUsernameCollectFinished = false
        val jobPass3of3 = insertNiceUsernameModern(
            dbHelper,
            DbTestHelpers.hearableANiceUsername
        ) { isNiceUsernameCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass3of3) { isNiceUsernameCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyNiceUsername)

        isNiceUsernameCollectFinished = false
        val jobGetAll = getAllNiceUsernames(dbHelper) { isNiceUsernameCollectFinished = true }
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
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        // Pass 1 of 3
        var isWearerIdCollectFinished = false
        val jobPass1of3 = insertWearerIdModern(
            dbHelper,
            DbTestHelpers.hearableAWearerId
        ) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isWearerIdCollectFinished }
        val idPriKeyCaptured = idPriKeyWearerId
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyWearerId)

        // Pass 2 of 3
        isWearerIdCollectFinished = false
        val jobPass2of3 = insertWearerIdModern(
            dbHelper,
            DbTestHelpers.hearableAWearerId
        ) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isWearerIdCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyWearerId)

        // Pass 3 of 3
        isWearerIdCollectFinished = false
        val jobPass3of3 = insertWearerIdModern(
            dbHelper,
            DbTestHelpers.hearableAWearerId
        ) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass3of3) { isWearerIdCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyWearerId)

        isWearerIdCollectFinished = false
        val jobGetAll = getAllWearerIds(dbHelper) { isWearerIdCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isWearerIdCollectFinished }
        assertEquals(1, listWearerIds?.size)
        ZepLog.d(TAG, "wearerIdInsert3TimesAndVerifyUnique(): $listWearerIds")
    }

    fun updateHearable(
        dbHelper: IDBImplHelper,
        hearableMacAddr: String,
        actionDone: () -> Unit
    ): Job {
        return GlobalScope.launch {
            val idPriKeyFlow = dbHelper.updateAuthenticateHearableIdFor(hearableMacAddr)
            idPriKeyFlow.collect { allHearables ->
                actionDone()
            }
        }
    }


    @Test
    fun updateAuthenticateHearable() {
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        var isHearableCollectFinished = false
        val jobPass1of3 = insertHearableIdModern(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableCollectFinished }
        val idPriKeyCaptured = idPriKeyHearable
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        isHearableCollectFinished = false
        val jobGetAll = getAllHearables(dbHelper) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableCollectFinished }
        assertEquals(false, listHearables?.get(0)?.isAuthenticated)
        ZepLog.d(TAG, "hearableIdInsert3TimesAndVerifyUnique(): $listHearables")

        isHearableCollectFinished = false
        val jobUpdate = updateHearable(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobUpdate) { isHearableCollectFinished }

        isHearableCollectFinished = false
        val jobGetAfterUpdate = getAllHearables(dbHelper) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAfterUpdate) { isHearableCollectFinished }
        assertEquals(true, listHearables?.get(0)?.isAuthenticated)
        ZepLog.d(TAG, "hearableIdInsert3TimesAndVerifyUnique(): $listHearables")
    }

    /**
     * Attempt to insert same NEC Hearable ID more than once will be ignored, but the correct ID will
     * be returned.
     */
    @Test
    fun hearableIdInsert3TimesAndVerifyUnique() {
        val dbHelper: IDBImplHelper = DBImplHelper(appContext, isTest = true)

        // Pass 1 of 3
        var isHearableCollectFinished = false
        val jobPass1of3 = insertHearableIdModern(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass1of3) { isHearableCollectFinished }
        val idPriKeyCaptured = idPriKeyHearable
        assertTrue(idPriKeyCaptured > 0)
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        // Pass 2 of 3
        isHearableCollectFinished = false
        val jobPass2of3 = insertHearableIdModern(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass2of3) { isHearableCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        // Pass 3 of 3
        isHearableCollectFinished = false
        val jobPass3of3 = insertHearableIdModern(
            dbHelper,
            DbTestHelpers.hearableAMacAddr
        ) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobPass3of3) { isHearableCollectFinished }
        assertEquals(idPriKeyCaptured, idPriKeyHearable)

        isHearableCollectFinished = false
        val jobGetAll = getAllHearables(dbHelper) { isHearableCollectFinished = true }
        DbTestHelpers.waitForCoroJob(jobGetAll) { isHearableCollectFinished }
        assertEquals(1, listHearables?.size)
        ZepLog.d(TAG, "hearableIdInsert3TimesAndVerifyUnique(): $listHearables")
    }
}
