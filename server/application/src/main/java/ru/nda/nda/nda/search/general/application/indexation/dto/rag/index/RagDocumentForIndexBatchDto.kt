package nda.search.general.application.indexation.dto.rag.index

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

data class RagDocumentForIndexBatchDto(
    @JsonProperty("service") val service: String,
    @JsonProperty("product") val product: String,
    @JsonProperty("meta") val meta: JsonNode = JsonNodeFactory.instance.objectNode(),
    @JsonProperty("index_version") val indexVersion: Long? = null,
    @JsonProperty("auto_switch") val autoSwitch: Boolean = true,
    @JsonProperty("diff") val diff: Boolean = false,
    @JsonProperty("documents") val documents: List<RagDocumentForIndexDto>,
)
