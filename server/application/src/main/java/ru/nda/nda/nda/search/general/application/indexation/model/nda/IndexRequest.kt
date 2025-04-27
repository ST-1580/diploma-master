package nda.search.general.application.indexation.model.nda

sealed interface IndexRequest {
    val catalogId: Long
    val clusterId: Long?
    val sectionId: Long?
    val knowledgeId: Long?
    val id: Long
    val text: String
}

data class BlockTranslationIndexRequest(
    override val catalogId: Long,
    override val clusterId: Long,
    override val sectionId: Long,
    override val knowledgeId: Long,
    override val id: Long,
    override val text: String = "",
) : IndexRequest
