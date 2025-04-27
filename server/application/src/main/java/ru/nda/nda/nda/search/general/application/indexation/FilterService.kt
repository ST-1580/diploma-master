package nda.search.general.application.indexation

import org.springframework.stereotype.Service
import ru.nda.api.integration.searchgeneral.dto.index.EntityFromNdaForIndexDto
import nda.search.general.domain.model.filter.ArrayFilter
import nda.search.general.domain.model.filter.FilterType
import nda.search.general.domain.model.filter.RagFilter
import nda.search.general.domain.model.filter.SimpleFilter
import nda.search.general.domain.repository.FilterRepository

@Service
class FilterService(
    private val filterRepository: FilterRepository,
) {
    fun getFilterFunctions(filterIdBySettingsId: Map<Long, Long>): Map<Long, (EntityFromNdaForIndexDto) -> Boolean> {
        val filterIds = filterIdBySettingsId.values.toSet()
        val filtersByFilterId = filterRepository.getFiltersById(filterIds).groupBy { it.filterId }

        val filterFunctionByFilterId = filtersByFilterId.mapValues { createFilterFunction(it.value) }

        return filterIdBySettingsId
            .mapValues { filterFunctionByFilterId[it.value] ?: { _: EntityFromNdaForIndexDto -> false } }
    }

    companion object {
        fun createFilterFunction(ragFilters: List<RagFilter>): (EntityFromNdaForIndexDto) -> Boolean {
            return { entity: EntityFromNdaForIndexDto ->
                var isUnderFilter = true
                ragFilters.forEach { ragFilter ->
                    val currCheck = when (ragFilter) {
                        is SimpleFilter<*> -> checkSimpleFilter(ragFilter, entity)

                        is ArrayFilter<*> -> checkArrayFilter(ragFilter, entity)

                        else -> false
                    }
                    isUnderFilter = isUnderFilter && currCheck
                }
                isUnderFilter
            }
        }

        fun <T> checkSimpleFilter(filter: SimpleFilter<T>, entity: EntityFromNdaForIndexDto): Boolean {
            return when (filter.value) {
                is Long -> {
                    when (filter.type) {
                        FilterType.TAG_ID_FOR_INDEX -> (entity.tagIds as Set<*>).contains(filter.value)
                        else -> false
                    }
                }
                else -> false
            }
        }

        fun <T> checkArrayFilter(filter: ArrayFilter<T>, entity: EntityFromNdaForIndexDto): Boolean {
            if (filter.value.isEmpty()) {
                return filter.type == FilterType.SECTION_IDS_NOT_FOR_INDEX
            }

            return when (filter.value.first()) {
                is Long -> {
                    when (filter.type) {
                        FilterType.CLUSTER_IDS_FOR_INDEX -> (filter.value as Set<*>).contains(entity.clusterId)
                        FilterType.SECTION_IDS_FOR_INDEX -> (filter.value as Set<*>).contains(entity.sectionId)
                        FilterType.SECTION_IDS_NOT_FOR_INDEX -> !(filter.value as Set<*>).contains(entity.sectionId)
                        else -> false
                    }
                }
                else -> false
            }
        }
    }
}
