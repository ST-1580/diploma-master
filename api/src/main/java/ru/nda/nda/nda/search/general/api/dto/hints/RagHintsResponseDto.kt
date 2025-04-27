package nda.search.general.api.dto.hints

import com.fasterxml.jackson.annotation.JsonProperty

data class RagHintsResponseDto(
    @JsonProperty("answers") val answers: List<AnswerDto>,
    @JsonProperty("x-request-id") val requestId: String,
)
