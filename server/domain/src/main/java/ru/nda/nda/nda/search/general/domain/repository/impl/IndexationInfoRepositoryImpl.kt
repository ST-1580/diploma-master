package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.INDEXATION_INFO
import nda.search.general.domain.db.Tables.RAG_INDEX_TO_INDEXATION_INFO
import nda.search.general.domain.db.tables.records.IndexationInfoRecord
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.IndexationEntityType
import nda.search.general.domain.model.IndexationInfo
import nda.search.general.domain.model.requests.IndexationCreateRq
import nda.search.general.domain.repository.IndexationInfoRepository

@Repository
class IndexationInfoRepositoryImpl @Autowired constructor(
    private val dslContext: DSLContext,
) : IndexationInfoRepository {
    override fun create(indexationCreateRq: IndexationCreateRq): IndexationInfo? {
        return dslContext
            .insertInto(INDEXATION_INFO, INDEXATION_INFO.LAST_INDEXED_ENTITY_TS, INDEXATION_INFO.ENTITY_TYPE)
            .values(indexationCreateRq.startedTs, indexationCreateRq.indexationEntityType.name)
            .returning()
            .firstOrNull()
            ?.let(::toModel)
    }

    override fun get(indexationId: Long): IndexationInfo? {
        return dslContext
            .selectFrom(INDEXATION_INFO)
            .where(INDEXATION_INFO.ID.eq(indexationId))
            .fetchOne()
            ?.let(::toModel)
    }

    override fun getAllActive(): List<IndexationInfo> {
        return dslContext
            .selectFrom(INDEXATION_INFO)
            .where(INDEXATION_INFO.ENABLED.eq(true))
            .fetch(::toModel)
    }

    override fun updateIndexationEnable(indexationId: Long, newEnabled: Boolean): IndexationInfo? {
        return dslContext
            .update(INDEXATION_INFO)
            .set(INDEXATION_INFO.ENABLED, newEnabled)
            .where(INDEXATION_INFO.ID.eq(indexationId))
            .returning()
            .firstOrNull()
            ?.let(::toModel)
    }

    override fun updateIndexationLastTs(indexationId: Long, newLastTs: Long): IndexationInfo? {
        return dslContext
            .update(INDEXATION_INFO)
            .set(INDEXATION_INFO.LAST_INDEXED_ENTITY_TS, newLastTs)
            .where(INDEXATION_INFO.ID.eq(indexationId))
            .returning()
            .firstOrNull()
            ?.let(::toModel)
    }

    override fun getLinkedRagIndexIds(indexationId: Long): List<Long> {
        return dslContext
            .select(RAG_INDEX_TO_INDEXATION_INFO.RAG_INDEX_ID)
            .from(RAG_INDEX_TO_INDEXATION_INFO)
            .where(
                RAG_INDEX_TO_INDEXATION_INFO.INDEXATION_INFO_ID.eq(indexationId)
                    .and(RAG_INDEX_TO_INDEXATION_INFO.STATUS.eq(ActivityStatus.ACTIVE.name))
            )
            .fetch(RAG_INDEX_TO_INDEXATION_INFO.RAG_INDEX_ID)
    }

    companion object {
        fun toModel(record: IndexationInfoRecord): IndexationInfo {
            return IndexationInfo(
                indexationId = record.id,
                lastIndexedEntityTs = record.lastIndexedEntityTs,
                indexationEntityType = IndexationEntityType.valueOf(record.entityType),
                enabled = record.enabled,
            )
        }
    }
}
