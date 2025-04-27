package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.RAG_INDEX
import nda.search.general.domain.db.Tables.RAG_INDEX_TO_NDA_CLUSTER
import nda.search.general.domain.db.tables.records.RagIndexRecord
import nda.search.general.domain.model.RagIndex
import nda.search.general.domain.repository.RagIndexRepository
import ru.nda.library.spring.boot.jdbc.postgres.cluster.starter.transaction.TransactionalHelper

@Repository
class RagIndexRepositoryImpl @Autowired constructor(
    private val dslContext: DSLContext,
    private val transactionalHelper: TransactionalHelper,
) : RagIndexRepository {

    override fun getById(ragIndexId: Long): RagIndex {
        return transactionalHelper.tx("RagIndexRepositoryImpl.getById") {
            val ragIndexInfo = dslContext
                .selectFrom(RAG_INDEX)
                .where(RAG_INDEX.ID.eq(ragIndexId))
                .fetchOne()
                ?: throw IllegalArgumentException(
                    "No such rag index with id $ragIndexId. Maybe you need to change indexation task properties"
                )

            val ndaClusterIds = dslContext
                .selectFrom(RAG_INDEX_TO_NDA_CLUSTER)
                .where(RAG_INDEX_TO_NDA_CLUSTER.RAG_INDEX_ID.eq(ragIndexId))
                .fetch(RAG_INDEX_TO_NDA_CLUSTER.NDA_CLUSTER_ID)

            with(ragIndexInfo) {
                RagIndex(
                    id = get(RAG_INDEX.ID),
                    serviceName = get(RAG_INDEX.SERVICE_NAME),
                    productName = get(RAG_INDEX.PRODUCT_NAME),
                    indexName = get(RAG_INDEX.INDEX_NAME),
                    ndaClusterIds = ndaClusterIds,
                )
            }
        }
    }

    override fun getRagIndexByIndexId(ragIndexIds: Collection<Long>): Map<Long, RagIndex> {
        return dslContext
            .selectFrom(RAG_INDEX)
            .where(RAG_INDEX.ID.`in`(ragIndexIds))
            .fetch()
            .associate { it.id to convertToModel(it) }
    }

    override fun getRagIndexByProductName(productNames: Collection<String>): Map<String, RagIndex> {
        return dslContext
            .selectFrom(RAG_INDEX)
            .where(RAG_INDEX.PRODUCT_NAME.`in`(productNames))
            .fetch()
            .associate { it.productName to convertToModel(it) }
    }

    override fun getClusterIdToRagIndexId(clusterIds: List<Long>): Map<Long, List<Long>> {
        return dslContext
            .selectFrom(RAG_INDEX_TO_NDA_CLUSTER)
            .where(RAG_INDEX_TO_NDA_CLUSTER.NDA_CLUSTER_ID.`in`(clusterIds))
            .fetchGroups(RAG_INDEX_TO_NDA_CLUSTER.NDA_CLUSTER_ID)
            .mapValues { it.value.map { record -> record.ragIndexId } }
    }

    companion object {
        private fun convertToModel(ragIndexInfo: RagIndexRecord): RagIndex {
            return with(ragIndexInfo) {
                RagIndex(
                    id = get(RAG_INDEX.ID),
                    serviceName = get(RAG_INDEX.SERVICE_NAME),
                    productName = get(RAG_INDEX.PRODUCT_NAME),
                    indexName = get(RAG_INDEX.INDEX_NAME),
                )
            }
        }
    }
}
