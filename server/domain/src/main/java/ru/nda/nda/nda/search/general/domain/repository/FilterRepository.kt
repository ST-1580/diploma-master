package nda.search.general.domain.repository

import nda.search.general.domain.model.filter.RagFilter

interface FilterRepository {
    fun getFiltersById(filterIds: Collection<Long>): List<RagFilter>
}
