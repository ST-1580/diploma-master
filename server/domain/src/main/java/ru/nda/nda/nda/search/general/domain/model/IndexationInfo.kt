package nda.search.general.domain.model

data class IndexationInfo(
    val indexationId: Long,
    val lastIndexedEntityTs: Long,
    val indexationEntityType: IndexationEntityType,
    val enabled: Boolean,
)
