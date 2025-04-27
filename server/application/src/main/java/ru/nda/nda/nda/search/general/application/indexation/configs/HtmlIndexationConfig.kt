package nda.search.general.application.indexation.configs

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto
import nda.search.general.application.integration.conversion.YfmConverterService

@Service
class HtmlIndexationConfig(
    private val yfmConverterService: YfmConverterService
) : IndexationConfigService {

    override fun getIndexBatch(knowledges: List<KnowledgeWithBlocksForIndexDto>): List<RagDocumentForIndexDto> {
        LOG.debug("Index batch via HTML_CONFIG preparing")

        val blocksMap = knowledges.flatMap { it.blocks }.associate { it.id.toString() to it.text }
        val htmlBlocks = yfmConverterService.yfm2html(blocksMap)

        return knowledges.map {
            RagDocumentForIndexDto(
                docId = it.knowledge.knowledgeId.toString(),
                title = it.knowledge.title,
                text = it.blocks.joinToString("\n") { block -> htmlBlocks[block.id.toString()] ?: "" },
                meta = JsonNodeFactory.instance.objectNode()
                    .put("cluster_id", it.knowledge.clusterId)
                    .put("knowledge_id", it.knowledge.knowledgeId)
                    .put("knowledge_name", it.knowledge.title)
            )
        }
    }

    private val LOG = LoggerFactory.getLogger(HtmlIndexationConfig::class.java)
}
