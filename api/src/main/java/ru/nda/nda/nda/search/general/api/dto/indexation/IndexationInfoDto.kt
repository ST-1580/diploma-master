package nda.search.general.api.dto.indexation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class IndexationInfoDto @JsonCreator constructor(

    @field:JsonProperty("indexationId", required = true)
    @get:JsonProperty("indexationId", required = true)
    @param:JsonProperty("indexationId", required = true)
    val indexationId: Long,

    @field:JsonProperty("lastIndexedEntityTs", required = true)
    @get:JsonProperty("lastIndexedEntityTs", required = true)
    @param:JsonProperty("lastIndexedEntityTs", required = true)
    val lastIndexedEntityTs: Long,

    @field:JsonProperty("indexationEntityType", required = true)
    @get:JsonProperty("indexationEntityType", required = true)
    @param:JsonProperty("indexationEntityType", required = true)
    val indexationEntityType: IndexationEntityTypeDto,

    @field:JsonProperty("enabled", required = true)
    @get:JsonProperty("enabled", required = true)
    @param:JsonProperty("enabled", required = true)
    val enabled: Boolean,
)
