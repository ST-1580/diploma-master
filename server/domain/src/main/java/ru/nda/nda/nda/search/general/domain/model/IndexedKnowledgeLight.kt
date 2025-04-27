package nda.search.general.domain.model

data class IndexedKnowledgeLight(
    val ragIndexId: Long,
    val knowledgeId: Long,
    val producedDocId: String,
)
