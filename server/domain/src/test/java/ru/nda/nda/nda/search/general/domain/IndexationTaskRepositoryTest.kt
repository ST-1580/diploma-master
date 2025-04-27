package nda.search.general.domain

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.IndexationTask
import nda.search.general.domain.model.requests.IndexationTaskStartRq
import nda.search.general.domain.model.requests.IndexationTasksCreateRq
import nda.search.general.domain.repository.IndexationTaskRepository
import kotlin.test.assertEquals

class IndexationTaskRepositoryTest : DomainTest {

    @Autowired
    private lateinit var indexationTaskRepository: IndexationTaskRepository

    @Test
    fun `test create`() {
        val indexationTasksCreateRq = IndexationTasksCreateRq(
            indexationId = 1L,
            startedTs = 100L,
            runners = listOf("testOne", "testTwo"),
        )

        val tasks = indexationTaskRepository.createTasks(indexationTasksCreateRq)
        assertEquals(2, tasks.size)

        val expectedTaskOne = IndexationTask(
            indexationId = indexationTasksCreateRq.indexationId,
            lastIndexedEntityTs = indexationTasksCreateRq.startedTs,
            runnerName = indexationTasksCreateRq.runners[0],
            entitiesWithIndexedTsCnt = 0,
            taskStatus = ActivityStatus.INACTIVE,
        )
        val expectedTaskTwo = expectedTaskOne.copy(runnerName = indexationTasksCreateRq.runners[1])

        assertEquals(expectedTaskOne, tasks[0])
        assertEquals(expectedTaskTwo, tasks[1])
    }

    @Test
    fun `test start`() {
        val indexationTasksCreateRq = IndexationTasksCreateRq(
            indexationId = 2L,
            startedTs = 100L,
            runners = listOf("testOne", "testTwo"),
        )

        val tasks = indexationTaskRepository.createTasks(indexationTasksCreateRq)
        assertEquals(2, tasks.size)

        val indexationTasksStartRqs = tasks.map { IndexationTaskStartRq(it) }
        indexationTaskRepository.startTasks(indexationTasksStartRqs)

        val expectedTaskOne = IndexationTask(
            indexationId = indexationTasksCreateRq.indexationId,
            lastIndexedEntityTs = indexationTasksCreateRq.startedTs,
            runnerName = indexationTasksCreateRq.runners[0],
            entitiesWithIndexedTsCnt = 0,
            taskStatus = ActivityStatus.ACTIVE,
        )
        val expectedTaskTwo = expectedTaskOne.copy(runnerName = indexationTasksCreateRq.runners[1])

        val indexations = indexationTaskRepository.getTasksByIndexationId(indexationTasksCreateRq.indexationId)
        assertEquals(2, indexations.size)

        assertEquals(expectedTaskOne, indexations[0])
        assertEquals(expectedTaskTwo, indexations[1])
    }
}
