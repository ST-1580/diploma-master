package nda.search.general.api.dto.hints

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

data class RagHintsRequestDto(
    @JsonProperty("product") val product: String,
    @JsonProperty("replies") val replies: Int,
    @JsonProperty("dialog") val dialog: List<DialogMessageDto>,
    @JsonProperty("meta_features") val metaFeatures: JsonNode = JsonNodeFactory.instance.objectNode(),
    @JsonProperty("options") val options: JsonNode = JsonNodeFactory.instance.objectNode(),
)
