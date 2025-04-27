package nda.search.general.application.indexation

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.springframework.stereotype.Service
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto
import nda.search.general.domain.model.IndexedKnowledgeLight
import nda.search.general.domain.repository.IndexedKnowledgesRepository

@Service
class IndexedKnowledgesService(
    private val indexedKnowledgesRepository: IndexedKnowledgesRepository,
) {

    fun markIndexed(indexedKnowledges: List<IndexedKnowledgeLight>) {
        indexedKnowledgesRepository.markIndexed(indexedKnowledges)
    }

    fun markDeleted(deletedKnowledges: List<IndexedKnowledgeLight>) {
        indexedKnowledgesRepository.markDeleted(deletedKnowledges)
    }

    fun getRagDocsByKnowledgeId(ragIndexId: Long, knowledgeIds: List<Long>): List<RagDocumentForIndexDto> {
        if (knowledgeIds.isEmpty()) {
            return emptyList()
        }

        val indexedKnowledges = indexedKnowledgesRepository.getIndexedByKnowledgeIds(ragIndexId, knowledgeIds)

        return indexedKnowledges.map {
            RagDocumentForIndexDto(
                docId = it.producedDocId,
                title = "",
                text = "",
                meta = JsonNodeFactory.instance.objectNode()
                    .put("knowledge_id", it.knowledgeId)
            )
        }
    }
}
