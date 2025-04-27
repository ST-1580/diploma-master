package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.INDEXED_KNOWLEDGES
import nda.search.general.domain.db.tables.records.IndexedKnowledgesRecord
import nda.search.general.domain.model.IndexedKnowledgeLight
import nda.search.general.domain.repository.IndexedKnowledgesRepository

@Repository
class IndexedKnowledgesRepositoryImpl(
    private val dslContext: DSLContext,
) : IndexedKnowledgesRepository {

    override fun markIndexed(indexedKnowledges: List<IndexedKnowledgeLight>) {
        val time = System.currentTimeMillis()

        dslContext.batch(
            indexedKnowledges
                .map(::convertToRecord)
                .map {
                    dslContext
                        .insertInto(INDEXED_KNOWLEDGES)
                        .set(it)
                        .onConflict(
                            INDEXED_KNOWLEDGES.RAG_INDEX_ID,
                            INDEXED_KNOWLEDGES.KNOWLEDGE_ID,
                            INDEXED_KNOWLEDGES.PRODUCED_DOC_ID
                        )
                        .doUpdate()
                        .set(INDEXED_KNOWLEDGES.STATUS, IN_INDEX)
                        .set(INDEXED_KNOWLEDGES.UPDATED_TS, time)
                }
        ).execute()
    }

    override fun markDeleted(deletedKnowledges: List<IndexedKnowledgeLight>) {
        var condition = DSL.condition(false)
        deletedKnowledges.forEach {
            condition = condition.or(
                INDEXED_KNOWLEDGES.RAG_INDEX_ID.eq(it.ragIndexId)
                    .and(INDEXED_KNOWLEDGES.KNOWLEDGE_ID.eq(it.knowledgeId))
                    .and(INDEXED_KNOWLEDGES.PRODUCED_DOC_ID.eq(it.producedDocId))
            )
        }

        val time = System.currentTimeMillis()
        dslContext
            .update(INDEXED_KNOWLEDGES)
            .set(INDEXED_KNOWLEDGES.STATUS, OUT_OF_INDEX)
            .set(INDEXED_KNOWLEDGES.UPDATED_TS, time)
            .where(condition)
            .execute()
    }

    override fun getIndexedByKnowledgeIds(ragIndexId: Long, knowledgeIds: List<Long>): List<IndexedKnowledgeLight> {
        return dslContext
            .selectFrom(INDEXED_KNOWLEDGES)
            .where(INDEXED_KNOWLEDGES.RAG_INDEX_ID.eq(ragIndexId))
            .and(INDEXED_KNOWLEDGES.KNOWLEDGE_ID.`in`(knowledgeIds))
            .and(INDEXED_KNOWLEDGES.STATUS.eq(IN_INDEX))
            .fetch(::toModelLight)
    }

    private fun convertToRecord(indexedKnowledge: IndexedKnowledgeLight): IndexedKnowledgesRecord {
        val record = IndexedKnowledgesRecord()

        record.ragIndexId = indexedKnowledge.ragIndexId
        record.knowledgeId = indexedKnowledge.knowledgeId
        record.producedDocId = indexedKnowledge.producedDocId
        record.status = IN_INDEX
        record.updatedTs = System.currentTimeMillis()

        return record
    }

    companion object {
        private const val IN_INDEX = "IN_INDEX"
        private const val OUT_OF_INDEX = "OUT_OF_INDEX"

        private fun toModelLight(record: IndexedKnowledgesRecord): IndexedKnowledgeLight {
            return with (record) {
                IndexedKnowledgeLight(
                    ragIndexId = get(INDEXED_KNOWLEDGES.RAG_INDEX_ID),
                    knowledgeId = get(INDEXED_KNOWLEDGES.KNOWLEDGE_ID),
                    producedDocId = get(INDEXED_KNOWLEDGES.PRODUCED_DOC_ID),
                )
            }
        }
    }
}
