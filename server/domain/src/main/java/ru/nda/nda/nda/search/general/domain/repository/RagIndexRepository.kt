package nda.search.general.domain.repository

import nda.search.general.domain.model.RagIndex

interface RagIndexRepository {
    fun getById(ragIndexId: Long): RagIndex

    fun getRagIndexByIndexId(ragIndexIds: Collection<Long>): Map<Long, RagIndex>

    fun getRagIndexByProductName(productNames: Collection<String>): Map<String, RagIndex>

    fun getClusterIdToRagIndexId(clusterIds: List<Long>): Map<Long, List<Long>>
}
