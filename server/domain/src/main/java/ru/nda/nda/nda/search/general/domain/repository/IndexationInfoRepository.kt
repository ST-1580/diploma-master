package nda.search.general.domain.repository

import nda.search.general.domain.model.IndexationInfo
import nda.search.general.domain.model.requests.IndexationCreateRq

interface IndexationInfoRepository {
    fun create(indexationCreateRq: IndexationCreateRq): IndexationInfo?

    fun get(indexationId: Long): IndexationInfo?

    fun getAllActive(): List<IndexationInfo>

    fun updateIndexationEnable(indexationId: Long, newEnabled: Boolean): IndexationInfo?

    fun updateIndexationLastTs(indexationId: Long, newLastTs: Long): IndexationInfo?

    fun getLinkedRagIndexIds(indexationId: Long): List<Long>
}
