package nda.search.general.application.indexation.dto.rag.answer

import com.fasterxml.jackson.annotation.JsonProperty

data class RagAnswerRpDto(
    @JsonProperty("answers") val answers: List<RagAnswerDto>,
)
