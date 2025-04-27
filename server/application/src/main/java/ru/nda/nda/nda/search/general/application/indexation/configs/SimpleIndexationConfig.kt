package nda.search.general.application.indexation.configs

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.slf4j.LoggerFactory
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import nda.search.general.application.indexation.dto.rag.index.RagDocumentForIndexDto

object SimpleIndexationConfig : IndexationConfigService {

    override fun getIndexBatch(knowledges: List<KnowledgeWithBlocksForIndexDto>): List<RagDocumentForIndexDto> {
        LOG.debug("Index batch via SIMPLE_CONFIG preparing")

        return knowledges.map {
            RagDocumentForIndexDto(
                docId = it.knowledge.knowledgeId.toString(),
                title = it.knowledge.title,
                text = it.blocks.joinToString("\n") { it.text },
                meta = JsonNodeFactory.instance.objectNode()
                    .put("cluster_id", it.knowledge.clusterId)
                    .put("knowledge_id", it.knowledge.knowledgeId)
                    .put("knowledge_name", it.knowledge.title)
            )
        }
    }
    private val LOG = LoggerFactory.getLogger(SimpleIndexationConfig::class.java)
}
