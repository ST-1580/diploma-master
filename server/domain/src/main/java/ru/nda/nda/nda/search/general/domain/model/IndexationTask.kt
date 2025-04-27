package nda.search.general.domain.model

data class IndexationTask(
    val indexationId: Long,
    val lastIndexedEntityTs: Long,
    val runnerName: String,
    val entitiesWithIndexedTsCnt: Int,
    val taskStatus: ActivityStatus,
)
