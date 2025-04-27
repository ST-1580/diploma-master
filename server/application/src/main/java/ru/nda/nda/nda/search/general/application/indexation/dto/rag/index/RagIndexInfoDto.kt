package nda.search.general.application.indexation.dto.rag.index

import com.fasterxml.jackson.annotation.JsonProperty

data class RagIndexInfoDto(
    @JsonProperty("index_name") val indexName: String,
    @JsonProperty("index_version") val indexVersion: Long,
)
