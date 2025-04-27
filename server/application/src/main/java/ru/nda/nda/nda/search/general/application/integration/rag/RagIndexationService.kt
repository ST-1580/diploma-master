package nda.search.general.application.integration.rag

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexBatchDto
import nda.search.general.application.indexation.dto.rag.index.RagIndexInfoDto
import nda.search.general.application.indexation.dto.rag.index.RagSwitchVersionRqDto

@RestController()
@RequestMapping(
    "/indexes/",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE]
)
interface RagIndexationService {

    @PostMapping("/{indexName}/documents")
    fun indexDocuments(
        @PathVariable("indexName") indexName: String,
        documents: RagDocumentForIndexBatchDto,
    ): RagIndexInfoDto

    @PostMapping("/{indexName}/switch_version")
    fun switchIndexVersion(
        @PathVariable("indexName") indexName: String,
        rq: RagSwitchVersionRqDto,
    ): RagIndexInfoDto

    @DeleteMapping("/{indexName}/documents")
    fun deleteDocuments(
        @PathVariable("indexName") indexName: String,
        @RequestParam("docs_ids") docUrls: List<String>,
        @RequestParam("product") product: String,
        @RequestParam("service") service: String,
    ): RagIndexInfoDto
}
