package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.FILTER_PROPERTY
import nda.search.general.domain.db.tables.records.FilterPropertyRecord
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.filter.ArrayFilter
import nda.search.general.domain.model.filter.FilterType
import nda.search.general.domain.model.filter.RagFilter
import nda.search.general.domain.model.filter.SimpleFilter
import nda.search.general.domain.repository.FilterRepository

@Repository
class FilterRepositoryImpl(
    private val dslContext: DSLContext,
) : FilterRepository {
    override fun getFiltersById(filterIds: Collection<Long>): List<RagFilter> {
        return dslContext
            .selectFrom(FILTER_PROPERTY)
            .where(FILTER_PROPERTY.FILTER_ID.`in`(filterIds).and(FILTER_PROPERTY.STATUS.eq(ActivityStatus.ACTIVE.name)))
            .fetch(::toModel)
    }

    companion object {
        private fun toModel(filterRecord: FilterPropertyRecord): RagFilter {
            return with(filterRecord) {
                val filterType = FilterType.valueOf(get(FILTER_PROPERTY.TYPE))
                when (filterType) {
                    FilterType.CLUSTER_IDS_FOR_INDEX,
                    FilterType.SECTION_IDS_FOR_INDEX,
                    FilterType.SECTION_IDS_NOT_FOR_INDEX -> {
                        ArrayFilter<Long>(
                            type = filterType,
                            filterId = get(FILTER_PROPERTY.FILTER_ID),
                            value = jsonToLongSet(get(FILTER_PROPERTY.VALUE).data())
                        )
                    }

                    FilterType.TAG_ID_FOR_INDEX -> {
                        SimpleFilter<Long>(
                            type = filterType,
                            filterId = get(FILTER_PROPERTY.FILTER_ID),
                            value = get(FILTER_PROPERTY.VALUE).data().toLong()
                        )
                    }
                }
            }
        }

        private fun jsonToLongSet(data: String): Set<Long> {
            return data.slice(1 until data.length - 1).split(",").map { it.trim().toLong() }.toSet()
        }
    }
}
