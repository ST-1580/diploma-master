package nda.search.general.application

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import nda.search.general.application.indexation.IndexationService
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.IndexationEntityType
import nda.search.general.domain.model.IndexationInfo
import nda.search.general.domain.model.requests.IndexationCreateRq
import nda.search.general.domain.repository.IndexationTaskRepository
import kotlin.test.assertEquals

class IndexationServiceTest : ApplicationTest {

    @Autowired
    private lateinit var indexationService: IndexationService

    @Autowired
    private lateinit var indexationTaskRepository: IndexationTaskRepository

    @Test
    fun `test create`() {
        val createRq = IndexationCreateRq(
            startedTs = 100L,
            indexationEntityType = IndexationEntityType.BLOCK
        )

        val indexationInfo = indexationService.create(createRq)
        val expected = IndexationInfo(
            indexationId = indexationInfo.indexationId,
            lastIndexedEntityTs = createRq.startedTs,
            indexationEntityType = createRq.indexationEntityType,
            enabled = false
        )

        assertEquals(expected, indexationInfo)
    }

    @Test
    fun `test enabled update`() {
        val createRq = IndexationCreateRq(
            startedTs = 100L,
            indexationEntityType = IndexationEntityType.BLOCK
        )
        val indexationInfo = indexationService.create(createRq)

        // start indexation
        var startedInfo = indexationService.updateIndexationEnable(indexationInfo.indexationId, true)
        assertEquals(true, startedInfo.enabled)
        var taskInfo = indexationTaskRepository.getTasksByIndexationId(indexationInfo.indexationId)
        assertEquals(3, taskInfo.size) // BLOCKS_FROM_BLOCK_TRANSLATION, BLOCKS_FROM_EMBEDDED_ENTITIES, BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK
        assertEquals(ActivityStatus.ACTIVE, taskInfo[0].taskStatus)

        // stop indexation
        startedInfo = indexationService.updateIndexationEnable(indexationInfo.indexationId, false)
        assertEquals(false, startedInfo.enabled)
        taskInfo = indexationTaskRepository.getTasksByIndexationId(indexationInfo.indexationId, ActivityStatus.INACTIVE)
        assertEquals(3, taskInfo.size) // BLOCKS_FROM_BLOCK_TRANSLATION, BLOCKS_FROM_EMBEDDED_ENTITIES, BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK
        assertEquals(ActivityStatus.INACTIVE, taskInfo[0].taskStatus)
    }

    @Test
    fun `test re indexation update`() {
        val createRq = IndexationCreateRq(
            startedTs = 100L,
            indexationEntityType = IndexationEntityType.BLOCK
        )
        val indexationInfo = indexationService.create(createRq)
        val reIndexationInfo = indexationService.create(createRq)

        // start indexation
        var startedInfo = indexationService.updateIndexationEnable(indexationInfo.indexationId, true)
        assertEquals(true, startedInfo.enabled)
        var taskInfo = indexationTaskRepository.getTasksByIndexationId(indexationInfo.indexationId)
        assertEquals(3, taskInfo.size) // BLOCKS_FROM_BLOCK_TRANSLATION, BLOCKS_FROM_EMBEDDED_ENTITIES, BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK
        assertEquals(ActivityStatus.ACTIVE, taskInfo[0].taskStatus)

        // start re indexation
        startedInfo = indexationService.updateIndexationEnable(reIndexationInfo.indexationId, true)
        assertEquals(true, startedInfo.enabled)
        taskInfo = indexationTaskRepository.getTasksByIndexationId(reIndexationInfo.indexationId)
        assertEquals(3, taskInfo.size) // BLOCKS_FROM_BLOCK_TRANSLATION, BLOCKS_FROM_EMBEDDED_ENTITIES, BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK
        assertEquals(ActivityStatus.ACTIVE, taskInfo[0].taskStatus)

        // stop re indexation
        startedInfo = indexationService.updateIndexationEnable(reIndexationInfo.indexationId, false)
        assertEquals(false, startedInfo.enabled)
        taskInfo = indexationTaskRepository.getTasksByIndexationId(reIndexationInfo.indexationId, ActivityStatus.INACTIVE)
        assertEquals(3, taskInfo.size) // BLOCKS_FROM_BLOCK_TRANSLATION, BLOCKS_FROM_EMBEDDED_ENTITIES, BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK
        assertEquals(ActivityStatus.INACTIVE, taskInfo[0].taskStatus)

        // check indexation still active
        taskInfo = indexationTaskRepository.getTasksByIndexationId(indexationInfo.indexationId)
        assertEquals(3, taskInfo.size) // BLOCKS_FROM_BLOCK_TRANSLATION, BLOCKS_FROM_EMBEDDED_ENTITIES, BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK
        assertEquals(ActivityStatus.ACTIVE, taskInfo[0].taskStatus)
    }
}
