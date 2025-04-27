package nda.search.general.application.integration.nda

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nda.api.integration.searchgeneral.dto.index.ActiveKnowledgeForIndexBatchDto
import ru.nda.api.integration.searchgeneral.dto.index.AllKnowledgesForIndexationSettingsRqDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockTranslationForIndexBatchDto
import ru.nda.api.integration.searchgeneral.dto.index.IndexationByKnowledgeIdsSettingsRqDto
import ru.nda.api.integration.searchgeneral.dto.index.IndexationSettingsRqDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeForIndexBatchDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexBatchDto

@RestController()
@RequestMapping(
    "/integration/search-general/entitiesForIndexing",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE]
)
interface EntitiesFromNdaReceiverService {

    @PostMapping("/allActiveKnowledges")
    fun getAllActiveKnowledges(
        request: AllKnowledgesForIndexationSettingsRqDto,
    ): ActiveKnowledgeForIndexBatchDto

    @PostMapping("/knowledgesWithBlocks/by/knowledgeIds")
    fun getKnowledgesWithBlocksByKnowledgeIds(
        request: IndexationByKnowledgeIdsSettingsRqDto,
    ): KnowledgeWithBlocksForIndexBatchDto

    @PostMapping("/blocks/by/blockTranslationVersion")
    fun getBlocksByBlockTranslationVersion(
        request: IndexationSettingsRqDto
    ): BlockTranslationForIndexBatchDto

    @PostMapping("/blocks/by/embeddedEntities")
    fun getBlocksByEmbeddedEntities(
        request: IndexationSettingsRqDto
    ): BlockTranslationForIndexBatchDto

    @PostMapping("/blocks/by/knowledgeBlockLink")
    fun getBlocksByKnowledgeBlockLink(
        request: IndexationSettingsRqDto
    ): BlockTranslationForIndexBatchDto

    @PostMapping("/knowledges/by/knowledgeProperty")
    fun getKnowledgesByKnowledgeProperty(
        request: IndexationSettingsRqDto
    ): KnowledgeForIndexBatchDto

    @PostMapping("/knowledges/by/knowledge")
    fun getKnowledgesByKnowledge(
        request: IndexationSettingsRqDto
    ): KnowledgeForIndexBatchDto
}
