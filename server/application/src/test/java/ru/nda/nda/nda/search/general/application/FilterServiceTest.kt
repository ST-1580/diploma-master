package nda.search.general.application

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeForIndexDto
import ru.nda.api.v1.dto.EntityStatusDto
import nda.search.general.application.indexation.FilterService
import nda.search.general.domain.model.filter.ArrayFilter
import nda.search.general.domain.model.filter.FilterType
import nda.search.general.domain.model.filter.SimpleFilter
import nda.search.general.domain.repository.FilterRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FilterServiceTest {
    private val filterRepository = mock<FilterRepository>()
    private val filterService = FilterService(filterRepository)

    @Test
    fun `test getFilterFunctions with valid filters`() {
        val filterIdBySettingsId = mapOf(1L to 101L, 2L to 102L)
        val ragFilters = listOf(
            SimpleFilter(FilterType.TAG_ID_FOR_INDEX, 101, 123L),
            ArrayFilter(FilterType.CLUSTER_IDS_FOR_INDEX, 102, setOf(456L))
        )
        whenever(filterRepository.getFiltersById(setOf(101L, 102L))).thenReturn(ragFilters)

        val filterFunctions = filterService.getFilterFunctions(filterIdBySettingsId)

        assertEquals(2, filterFunctions.size)

        val entity = KnowledgeForIndexDto(
            tagIds = setOf(123L),
            clusterId = 456L,
            sectionId = 789L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )

        assertTrue(filterFunctions[1]?.invoke(entity) == true)
        assertTrue(filterFunctions[2]?.invoke(entity) == true)
    }

    @Test
    fun `test getFilterFunctions with invalid filterId`() {
        val filterIdBySettingsId = mapOf(1L to 101L, 2L to 999L)
        val ragFilters = listOf(SimpleFilter(FilterType.TAG_ID_FOR_INDEX, 101, 123L))
        whenever(filterRepository.getFiltersById(setOf(101L, 999L))).thenReturn(ragFilters)

        val filterFunctions = filterService.getFilterFunctions(filterIdBySettingsId)

        assertEquals(2, filterFunctions.size)

        val entity = KnowledgeForIndexDto(
            tagIds = setOf(123L),
            clusterId = 456L,
            sectionId = 789L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )

        assertTrue(filterFunctions[1]?.invoke(entity) == true)
        assertTrue(filterFunctions[2]?.invoke(entity) == false)
    }

    @Test
    fun `test createFilterFunction with SimpleFilter`() {
        val ragFilters = listOf(SimpleFilter(FilterType.TAG_ID_FOR_INDEX, 1, 123L))

        val filterFunction = FilterService.createFilterFunction(ragFilters)

        val entityWithTag = KnowledgeForIndexDto(
            tagIds = setOf(123L),
            clusterId = 456L,
            sectionId = 789L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )
        val entityWithoutTag = KnowledgeForIndexDto(
            tagIds = setOf(456L),
            clusterId = 456L,
            sectionId = 789L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )

        assertTrue(filterFunction(entityWithTag))
        assertFalse(filterFunction(entityWithoutTag))
    }

    @Test
    fun `test createFilterFunction with ArrayFilter`() {
        val ragFilters = listOf(ArrayFilter(FilterType.CLUSTER_IDS_FOR_INDEX, 1, setOf(456L)))

        val filterFunction = FilterService.createFilterFunction(ragFilters)

        val entityWithCluster = KnowledgeForIndexDto(
            tagIds = emptySet(),
            clusterId = 456L,
            sectionId = 789L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )
        val entityWithoutCluster = KnowledgeForIndexDto(
            tagIds = emptySet(),
            clusterId = 789L,
            sectionId = 789L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )

        assertTrue(filterFunction(entityWithCluster))
        assertFalse(filterFunction(entityWithoutCluster))
    }

    @Test
    fun `test checkArrayFilter with SECTION_IDS_NOT_FOR_INDEX`() {
        val filter = ArrayFilter(FilterType.SECTION_IDS_NOT_FOR_INDEX, 1, setOf(456L))
        val entityWithSection = KnowledgeForIndexDto(
            sectionId = 456L,
            tagIds = emptySet(),
            clusterId = 456L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )
        val entityWithoutSection = KnowledgeForIndexDto(
            sectionId = 789L,
            tagIds = emptySet(),
            clusterId = 456L,
            triggeredUpdatedTs = 1,
            knowledgeId = 1,
            title = "",
            status = EntityStatusDto.ACTIVE,
        )

        assertFalse(FilterService.checkArrayFilter(filter, entityWithSection))
        assertTrue(FilterService.checkArrayFilter(filter, entityWithoutSection))
    }
}
