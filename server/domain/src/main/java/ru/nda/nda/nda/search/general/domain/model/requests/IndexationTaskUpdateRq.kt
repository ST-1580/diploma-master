package nda.search.general.domain.model.requests

data class IndexationTaskUpdateRq(
    val lastIndexedEntityTs: Long,
    val entitiesWithIndexedTsCnt: Int,
)
