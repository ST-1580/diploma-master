package nda.search.general.application.indexation.dto.rag.answer.score

import com.fasterxml.jackson.annotation.JsonProperty

data class RagScoreDto(
    @JsonProperty("status") val status: String = "NOT_AVAILABLE",
    @JsonProperty("relevance") val relevance: String = "NOT_AVAILABLE",
    @JsonProperty("modified_text") val modifiedText: String? = null,
    @JsonProperty("comment") val comment: String? = null
)
