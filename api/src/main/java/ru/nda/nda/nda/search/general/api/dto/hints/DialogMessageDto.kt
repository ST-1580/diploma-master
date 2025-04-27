package nda.search.general.api.dto.hints

import com.fasterxml.jackson.annotation.JsonProperty

data class DialogMessageDto(
    @JsonProperty("text") val text: String,
    @JsonProperty("role") val role: String,
    @JsonProperty("id") val id: Long? = null,
    @JsonProperty("created") val created: String? = null,
)
