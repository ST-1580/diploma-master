package nda.search.general.domain.model.filter

data class ArrayFilter<T>(
    override val type: FilterType,
    override val filterId: Long,
    val value: Set<T>,
) : RagFilter
