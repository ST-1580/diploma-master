package nda.search.general.api.dto.hints

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

data class AnswerDto(
    @JsonProperty("answer") val answer: String,
    @JsonProperty("meta") val meta: JsonNode = JsonNodeFactory.instance.objectNode(),
)
