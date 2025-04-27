package nda.search.general.api.dto.indexation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class IndexationCreateRqDto @JsonCreator constructor(

    @field:JsonProperty("startedTs", required = true)
    @get:JsonProperty("startedTs", required = true)
    @param:JsonProperty("startedTs", required = true)
    val startedTs: Long,

    @field:JsonProperty("indexationEntityType", required = true)
    @get:JsonProperty("indexationEntityType", required = true)
    @param:JsonProperty("indexationEntityType", required = true)
    val indexationEntityType: IndexationEntityTypeDto
)
