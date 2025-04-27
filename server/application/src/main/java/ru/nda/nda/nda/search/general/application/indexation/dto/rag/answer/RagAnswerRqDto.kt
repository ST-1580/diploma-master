package nda.search.general.application.indexation.dto.rag.answer

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory

data class RagAnswerRqDto(
    @JsonProperty("service") val service: String,
    @JsonProperty("product") val product: String,
    @JsonProperty("index_name") val indexName: String,
    @JsonProperty("replies") val replies: Int,
    @JsonProperty("dialog") val dialog: List<RagDialogDto>,
    @JsonProperty("meta_features") val metaFeatures: JsonNode = JsonNodeFactory.instance.objectNode(),
    @JsonProperty("options") val options: JsonNode = JsonNodeFactory.instance.objectNode(),

)
