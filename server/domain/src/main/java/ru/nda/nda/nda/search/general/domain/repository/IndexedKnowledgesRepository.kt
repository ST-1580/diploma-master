package nda.search.general.domain.repository

import nda.search.general.domain.model.IndexedKnowledgeLight

interface IndexedKnowledgesRepository {
    fun markIndexed(indexedKnowledges: List<IndexedKnowledgeLight>)

    fun markDeleted(deletedKnowledges: List<IndexedKnowledgeLight>)

    fun getIndexedByKnowledgeIds(ragIndexId: Long, knowledgeIds: List<Long>): List<IndexedKnowledgeLight>
}
