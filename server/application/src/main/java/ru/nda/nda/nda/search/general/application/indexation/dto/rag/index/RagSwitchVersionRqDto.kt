package nda.search.general.application.indexation.dto.rag.index

import com.fasterxml.jackson.annotation.JsonProperty

data class RagSwitchVersionRqDto(
    @JsonProperty("service") val service: String,
    @JsonProperty("product") val product: String,
    @JsonProperty("index_version") val indexVersion: Long,
)
