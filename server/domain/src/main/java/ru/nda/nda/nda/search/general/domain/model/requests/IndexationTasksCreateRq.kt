package nda.search.general.domain.model.requests

data class IndexationTasksCreateRq(
    val indexationId: Long,
    val startedTs: Long,
    val runners: List<String>
)
