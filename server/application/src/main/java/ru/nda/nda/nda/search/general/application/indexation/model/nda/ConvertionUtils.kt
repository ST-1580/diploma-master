package nda.search.general.application.indexation.model.nda

import ru.nda.api.integration.searchgeneral.dto.index.BlockTranslationForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto

fun toRagIndexDto(dto: KnowledgeWithBlocksForIndexDto): RagDocumentForIndexDto {
    return RagDocumentForIndexDto(
        docId = dto.knowledge.knowledgeId.toString(),
        title = dto.knowledge.title,
        text = dto.blocks.joinToString(separator = "\n") { it.text },
    )
}

fun toIndexRequest(dto: BlockTranslationForIndexDto): BlockTranslationIndexRequest {
    return BlockTranslationIndexRequest(
        catalogId = dto.catalogId,
        clusterId = dto.clusterId,
        sectionId = dto.sectionId,
        knowledgeId = dto.knowledgeId,
        id = dto.id,
        text = dto.text,
    )
}

fun toDeleteRequest(dto: BlockTranslationForIndexDto): BlockTranslationIndexRequest {
    return BlockTranslationIndexRequest(
        catalogId = dto.catalogId,
        clusterId = dto.clusterId,
        sectionId = dto.sectionId,
        knowledgeId = dto.knowledgeId,
        id = dto.id,
    )
}
