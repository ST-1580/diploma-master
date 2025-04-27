package nda.search.general.web.api.myself

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class MyselfResponseDto @JsonCreator constructor(

    @field:JsonProperty("principal")
    @get:JsonProperty("principal")
    @param:JsonProperty("principal")
    val principal: String?,

    @field:JsonProperty("system")
    @get:JsonProperty("system")
    @param:JsonProperty("system")
    val system: String?,

    @field:JsonProperty("authority")
    @get:JsonProperty("authority")
    @param:JsonProperty("authority")
    val authority: String?,
)
