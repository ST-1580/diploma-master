package nda.search.general.domain.model.filter

data class SimpleFilter<T> (
    override val type: FilterType,
    override val filterId: Long,
    val value: T,
) : RagFilter
