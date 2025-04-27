package nda.search.general.application.indexation.configs

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.slf4j.LoggerFactory
import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockTranslationForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import ru.nda.api.v1.dto.BlockTypeDto
import ru.nda.api.v1.dto.ChannelTypeDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto

data object FunTechIndexationConfig : IndexationConfigService {
    override fun getIndexBatch(knowledges: List<KnowledgeWithBlocksForIndexDto>): List<RagDocumentForIndexDto> {
        LOG.debug("Index batch via FUN_TECH_CONFIG preparing")

        return knowledges.mapNotNull { knowledge ->
            if (knowledge.knowledge.title.contains("[i]")) {
                return@mapNotNull null
            }

            var blockTitle = ""
            var workingBlocks = emptyList<BlockTranslationForIndexDto>()

            val solutionBlocks = knowledge.blocks.filter { it.blockType == BlockTypeDto.SOLUTION }
            val problemBlocks = knowledge.blocks.filter { it.blockType == BlockTypeDto.PROBLEM }
            val utilBlocks = knowledge.blocks.filter { it.blockType == BlockTypeDto.UTIL }
            if (solutionBlocks.isNotEmpty()) {
                val filteredData = filterBlocks(solutionBlocks, "Решение")
                blockTitle = filteredData.first
                workingBlocks = filteredData.second
            } else if (problemBlocks.isNotEmpty()) {
                val filteredData = filterBlocks(problemBlocks, "Проблема")
                blockTitle = filteredData.first
                workingBlocks = filteredData.second
            } else if (utilBlocks.isNotEmpty()) {
                val filteredData = filterBlocks(utilBlocks, "Дополнительная информация")
                blockTitle = filteredData.first
                workingBlocks = filteredData.second
            } else {
                return@mapNotNull null
            }

            val blockText = workingBlocks.joinToString("\n\n") { block ->
                getText(
                    block = block,
                    startIndex = 0,
                    endIndex = block.text.length,
                )
            }

            val documentTitle = getDocumentTitle(knowledgeTitle = knowledge.knowledge.title)
            RagDocumentForIndexDto(
                docId = getDocumentId(knowledgeId = knowledge.knowledge.knowledgeId),
                title = documentTitle,
                text = getDocumentText(documentTitle = documentTitle, blockTitle = blockTitle, problemText = blockText),
                meta = getDocumentMeta(knowledge = knowledge)
            )
        }
    }

    private fun filterBlocks(
        blocks: List<BlockTranslationForIndexDto>,
        defaultTitle: String,
    ): Pair<String, List<BlockTranslationForIndexDto>> {
        var blockTitle = ""
        var workingBlocks = emptyList<BlockTranslationForIndexDto>()

        val mlBlocks = blocks.filter { it.channelType == ChannelTypeDto.ML }
        val chatBlocks = blocks.filter { it.channelType == ChannelTypeDto.CHAT }
        if (mlBlocks.isNotEmpty()) {
            blockTitle = "$defaultTitle ML"
            workingBlocks = mlBlocks
        } else if (chatBlocks.isNotEmpty()) {
            blockTitle = "$defaultTitle Чаты"
            workingBlocks = chatBlocks
        } else {
            blockTitle = defaultTitle
            workingBlocks = blocks
        }

        return blockTitle to workingBlocks
    }

    private fun getText(
        block: BlockTranslationForIndexDto,
        startIndex: Int,
        endIndex: Int,
    ): String {
        val text = block.text.substring(startIndex, endIndex)
        val sortedPatterns = block.embeddedEntities.batch.sortedBy { it.startPosition }

        var from = 0
        var to = 0
        var finalText = ""
        val shift = startIndex
        sortedPatterns.forEach { pattern ->
            to = pattern.startPosition - shift
            finalText += text.substring(from until to)
            finalText += convertPatternToIndexation(pattern)
            from = pattern.startPosition + pattern.length - shift
        }
        finalText += text.substring(from until text.length)

        return finalText
    }

    private fun convertPatternToIndexation(pattern: BlockEmbeddedEntityForIndexDto): String {
        return "<template>" +
            "<template_title>${pattern.title}</template_title>" +
            pattern.text +
            "</template>"
    }

    private fun getDocumentId(knowledgeId: Long): String {
        return "$knowledgeId"
    }

    private fun getDocumentTitle(knowledgeTitle: String): String {
        return knowledgeTitle
    }

    private fun getDocumentText(
        documentTitle: String,
        blockTitle: String,
        problemText: String,
    ): String {
        return "$documentTitle\n\n$blockTitle\n\n$problemText"
    }

    private fun getDocumentMeta(
        knowledge: KnowledgeWithBlocksForIndexDto,
    ): JsonNode {
        return JsonNodeFactory.instance.objectNode()
            .put("cluster_id", knowledge.knowledge.clusterId)
            .put("cluster_name", "Фантех")
            .put("knowledge_id", knowledge.knowledge.knowledgeId)
            .put("knowledge_name", knowledge.knowledge.title)
    }

    private val LOG = LoggerFactory.getLogger(FunTechIndexationConfig::class.java)
}
