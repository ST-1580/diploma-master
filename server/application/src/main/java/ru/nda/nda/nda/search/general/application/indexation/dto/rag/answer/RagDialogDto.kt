package nda.search.general.application.indexation.dto.rag.answer

import com.fasterxml.jackson.annotation.JsonProperty

data class RagDialogDto(
    @JsonProperty("text") val text: String,
    @JsonProperty("role") val role: String,
    @JsonProperty("id") val id: Long? = null,
    @JsonProperty("created") val created: String? = null,
)
