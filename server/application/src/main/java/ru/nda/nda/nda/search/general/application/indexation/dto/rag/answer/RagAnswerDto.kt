package nda.search.general.application.indexation.dto.rag.answer

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

data class RagAnswerDto(
    @JsonProperty("answer") val answer: String,
    @JsonProperty("meta") val meta: JsonNode = JsonNodeFactory.instance.objectNode(),
)
