package nda.search.general.domain.model.filter

sealed interface RagFilter {
    val type: FilterType
    val filterId: Long
}
