package nda.search.general.application.indexation.configs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.slf4j.LoggerFactory
import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityTypeDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockTranslationForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import ru.nda.api.v1.dto.BlockTypeDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto

data object TravelIndexationConfig : IndexationConfigService {
    override fun getIndexBatch(knowledges: List<KnowledgeWithBlocksForIndexDto>): List<RagDocumentForIndexDto> {
        LOG.debug("Index batch via TRAVEL_CONFIG preparing")

        return knowledges.flatMap { knowledge ->
            val problemBlocks = knowledge.blocks.filter { it.blockType == BlockTypeDto.PROBLEM }
            val problemText = getProblemText(problemBlocks = problemBlocks)

            knowledge.blocks.flatMap { block ->
                block.embeddedEntities.batch
                    .filter { it.type == BlockEmbeddedEntityTypeDto.SAMSARA_PATTERN }
                    .map { pattern ->
                        RagDocumentForIndexDto(
                            docId = getDocumentId(knowledgeId = knowledge.knowledge.knowledgeId, patternId = pattern.id),
                            title = getDocumentTitle(knowledgeTitle = knowledge.knowledge.title, patternTitle = pattern.title),
                            text = getDocumentText(knowledgeTitle = knowledge.knowledge.title, problemText = problemText, pattern = pattern),
                            meta = getDocumentMeta(knowledge = knowledge, pattern = pattern)
                        )
                    }
            }
        }
    }

    private fun getProblemText(problemBlocks: List<BlockTranslationForIndexDto>): String {
        return problemBlocks.joinToString("\n") { problemBlock ->
            val text = problemBlock.text
            val sortedPatterns = problemBlock.embeddedEntities.batch.sortedBy { it.startPosition }

            var from = 0
            var to = 0
            var finalText = ""
            sortedPatterns.forEach { pattern ->
                to = pattern.startPosition
                finalText += text.substring(from until to)
                finalText += convertPatternToIndexation(pattern)
                from = pattern.startPosition + pattern.length
            }
            finalText += text.substring(from until text.length)

            finalText
        }
    }

    private fun convertPatternToIndexation(pattern: BlockEmbeddedEntityForIndexDto): String {
        return "<template>" +
            "<template_title>${pattern.title}</template_title>" +
            pattern.text +
            "</template>"
    }

    private fun getDocumentId(knowledgeId: Long, patternId: String): String {
        return "${knowledgeId}_$patternId"
    }

    private fun getDocumentTitle(knowledgeTitle: String, patternTitle: String): String {
        return "$knowledgeTitle -> $patternTitle"
    }

    private fun getDocumentText(
        knowledgeTitle: String,
        problemText: String,
        pattern: BlockEmbeddedEntityForIndexDto,
    ): String {
        return "$knowledgeTitle\n\n$problemText\n\n${pattern.title}\n\n${pattern.text}"
    }

    private fun getDocumentMeta(
        knowledge: KnowledgeWithBlocksForIndexDto,
        pattern: BlockEmbeddedEntityForIndexDto,
    ): JsonNode {
        return JsonNodeFactory.instance.objectNode()
            .put("cluster_id", knowledge.knowledge.clusterId)
            .put("cluster_name", "Путешествия")
            .put("knowledge_id", knowledge.knowledge.knowledgeId)
            .put("knowledge_name", knowledge.knowledge.title)
            .put("pattern_id", pattern.id)
            .put("pattern_name", pattern.title)
    }

    private val LOG = LoggerFactory.getLogger(TravelIndexationConfig::class.java)
}
