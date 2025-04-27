package nda.search.general.application.indexation

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import nda.search.general.application.exception.HintsException
import nda.search.general.application.indexation.dto.rag.answer.RagAnswerRpDto
import nda.search.general.application.indexation.dto.rag.answer.RagAnswerRqDto
import nda.search.general.application.indexation.dto.rag.answer.score.RagScoreRqDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexBatchDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto
import nda.search.general.application.indexation.dto.rag.index.RagIndexInfoDto
import nda.search.general.application.integration.rag.RagAnswerService
import nda.search.general.application.integration.rag.RagIndexationService
import nda.search.general.domain.model.IndexedKnowledgeLight
import nda.search.general.domain.model.RagIndex
import nda.search.general.domain.repository.RagIndexRepository
import ru.nda.library.spring.boot.jackson.mapper.starter.DefaultObjectMapper

@Service
class RagService(
    private val ragIndexationService: RagIndexationService,
    private val ragAnswerService: RagAnswerService,
    private val indexedKnowledgesService: IndexedKnowledgesService,

    private val ragIndexRepository: RagIndexRepository,

    @Value("\${nda.search.general.indexing.rag.service.name:test_in_prod}")
    private val ragServiceName: String,

    @Value("\${nda.search.general.indexing.rag.index.name.prefix:nda_test}")
    private val ragIndexNamePrefix: String,
) {
    fun initRagIndex(ragIndexId: Long) {
        LOG.debug("Want to init $ragIndexId rag index")

        val ragIndexByRagIndexId = ragIndexRepository.getRagIndexByIndexId(ragIndexIds = listOf(ragIndexId))
        val ragIndex = ragIndexByRagIndexId[ragIndexId] ?: throw IllegalArgumentException("No such rag index $ragIndexId")

        val indexationResult = indexDocuments(ragIndex = ragIndex, diff = false)

        LOG.debug("Indexation result is $indexationResult")
    }

    fun indexDocuments(
        ragIndex: RagIndex,
        documents: List<RagDocumentForIndexDto> = emptyList(),
        diff: Boolean = true,
    ): RagIndexInfoDto {
        val docsForIndex = filterDocsInOneBatch(documents)

        val ragIndexRequest = RagDocumentForIndexBatchDto(
            service = getServiceName(ragIndex),
            product = ragIndex.productName,
            diff = diff,
            documents = docsForIndex,
        )
        val indexResult = ragIndexationService.indexDocuments(
            indexName = getIndexName(ragIndex),
            documents = ragIndexRequest
        )

        val indexedKnowledges = docsForIndex.map { convertDocToIndexedInfo(ragIndex.id, it) }
        indexedKnowledgesService.markIndexed(indexedKnowledges)

        return indexResult
    }

    fun deleteDocuments(
        ragIndex: RagIndex,
        documents: List<RagDocumentForIndexDto> = emptyList(),
    ): RagIndexInfoDto {
        val deleteResult = ragIndexationService.deleteDocuments(
            indexName = getIndexName(ragIndex),
            docUrls = documents.map { it.docId },
            product = ragIndex.productName,
            service = getServiceName(ragIndex),
        )

        val deletedKnowledges = documents.map { convertDocToIndexedInfo(ragIndex.id, it) }
        indexedKnowledgesService.markDeleted(deletedKnowledges)

        return deleteResult
    }

    private fun filterDocsInOneBatch(documents: List<RagDocumentForIndexDto>): List<RagDocumentForIndexDto> {
        val groupedDocs = documents.groupBy { it.docId }
        val badDocs = groupedDocs.filter { it.value.size > 1 }.keys
        if (badDocs.isNotEmpty()) {
            LOG.warn("Duplicate docs with ids $badDocs")
        }

        val okDocs = groupedDocs.mapNotNull { it.value.firstOrNull() }
        val emptyDocs = okDocs.filter { it.text.isEmpty() }
        if (emptyDocs.isNotEmpty()) {
            LOG.warn("Empty docs with ids ${emptyDocs.map { it.docId }}")
        }
        return okDocs - emptyDocs
    }

    fun getHints(rq: RagAnswerRqDto): Pair<String, RagAnswerRpDto> {
        val product = rq.product

        val ragIndex = ragIndexRepository.getRagIndexByProductName(listOf(product))[product]
            ?: throw HintsException.ProductNotFoundException(product)

        val indexName = if (rq.product == "funtech") {
            LOG.debug("Setted index name to crowd_funtech_rag")
            "crowd_funtech_rag"
        } else {
            getIndexName(ragIndex)
        }

        val response = ragAnswerService.answer(
            rq = rq.copy(
                service = getServiceName(ragIndex),
                indexName = indexName,
            )
        )

        val body = OBJECT_MAPPER.readValue(response.body().asInputStream(), RagAnswerRpDto::class.java)
        val requestId = response.headers()["x-request-id"]?.first() ?: DEFAULT_X_REQUEST_ID

        return requestId to body
    }

    fun score(rq: RagScoreRqDto) {
        val product = rq.product
        val ragIndex = ragIndexRepository.getRagIndexByProductName(listOf(product))[product]
            ?: throw HintsException.ProductNotFoundException(product)

        ragAnswerService.score(
            rq = rq.copy(
                service = getServiceName(ragIndex),
            )
        )
    }

    private fun getServiceName(ragIndex: RagIndex): String {
        return if (ragIndex.serviceName == DEFAULT_NDA_SERVICE_NAME) {
            ragServiceName
        } else {
            ragIndex.serviceName
        }
    }

    private fun getIndexName(ragIndex: RagIndex): String {
        return if (ragIndex.indexName == null) {
            "${ragIndexNamePrefix}_${ragIndex.id}"
        } else {
            ragIndex.indexName!!
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RagService::class.java)
        private val OBJECT_MAPPER = DefaultObjectMapper()
        private const val DEFAULT_X_REQUEST_ID = "NONE"
        private const val DEFAULT_NDA_SERVICE_NAME = "nda"

        private fun convertDocToIndexedInfo(ragIndexId: Long, doc: RagDocumentForIndexDto): IndexedKnowledgeLight {
            return IndexedKnowledgeLight(
                ragIndexId = ragIndexId,
                knowledgeId = doc.meta["knowledge_id"].longValue(),
                producedDocId = doc.docId,
            )
        }
    }
}
