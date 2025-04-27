package nda.search.general.api.dto.hints.score

import com.fasterxml.jackson.annotation.JsonProperty

data class ScoreDto(
    @JsonProperty("status") val status: String = "NOT_AVAILABLE",
    @JsonProperty("relevance") val relevance: String = "NOT_AVAILABLE",
    @JsonProperty("modified_text") val modifiedText: String? = null,
    @JsonProperty("comment") val comment: String? = null
)
