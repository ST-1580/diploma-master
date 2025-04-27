package nda.search.general.application.indexation.dto.rag.index

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

data class RagDocumentForIndexDto(
    @JsonProperty("doc_id") val docId: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("text") val text: String,
    @JsonProperty("meta") val meta: JsonNode = JsonNodeFactory.instance.objectNode(), // must have field "knowledge_id"
)
