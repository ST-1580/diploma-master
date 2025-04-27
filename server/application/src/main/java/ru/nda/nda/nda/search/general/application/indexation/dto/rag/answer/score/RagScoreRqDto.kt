package nda.search.general.application.indexation.dto.rag.answer.score

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import nda.search.general.application.indexation.dto.rag.answer.RagDialogDto

data class RagScoreRqDto(
    @JsonProperty("service") val service: String,
    @JsonProperty("product") val product: String,
    @JsonProperty("dialog") val dialog: List<RagDialogDto>,
    @JsonProperty("meta_features") val metaFeatures: JsonNode = JsonNodeFactory.instance.objectNode(),
    @JsonProperty("reply") val reply: String? = null,
    @JsonProperty("score") val score: RagScoreDto,
    @JsonProperty("ticketId") val ticketId: String? = null,
    @JsonProperty("scored_request_id") val scoredRequestId: String? = null,
    @JsonProperty("prompt") val prompt: String? = null
)
