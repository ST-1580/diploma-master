package nda.search.general.api.dto.hints.score

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import nda.search.general.api.dto.hints.DialogMessageDto

data class RagHintScoreRequestDto(
    @JsonProperty("product") val product: String,
    @JsonProperty("dialog") val dialog: List<DialogMessageDto>,
    @JsonProperty("meta_features") val metaFeatures: JsonNode = JsonNodeFactory.instance.objectNode(),
    @JsonProperty("reply") val reply: String? = null,
    @JsonProperty("score") val score: ScoreDto,
    @JsonProperty("ticketId") val ticketId: String? = null,
    @JsonProperty("scored_request_id") val scoredRequestId: String? = null,
    @JsonProperty("prompt") val prompt: String? = null
)
