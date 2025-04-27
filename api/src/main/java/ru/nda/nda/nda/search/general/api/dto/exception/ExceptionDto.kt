package nda.search.general.api.dto.exception

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeId
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed interface ExceptionDto {

    val requestId: String

    val message: String

    @get:JsonTypeId
    val type: ExceptionTypeDto
}

@JsonTypeName("INDEXATION_NOT_FOUND_EXCEPTION")
data class IndexationNotFoundExceptionDto @JsonCreator constructor(

    @field:JsonProperty("indexationId")
    @get:JsonProperty("indexationId")
    @param:JsonProperty("indexationId")
    val indexationId: Long,

    @field:JsonProperty("requestId")
    @get:JsonProperty("requestId")
    @param:JsonProperty("requestId")
    override val requestId: String,
) : ExceptionDto {

    @field:JsonProperty("message")
    @get:JsonProperty("message")
    override val message: String = "Not found indexation with id = '$indexationId'"

    @field:JsonProperty("type")
    @get:JsonProperty("type")
    override val type: ExceptionTypeDto = ExceptionTypeDto.INDEXATION_NOT_FOUND_EXCEPTION
}

@JsonTypeName("CANNOT_CREATE_INDEXATION_EXCEPTION")
data class CannotCreateIndexationExceptionDto @JsonCreator constructor(

    @field:JsonProperty("requestId")
    @get:JsonProperty("requestId")
    @param:JsonProperty("requestId")
    override val requestId: String,
) : ExceptionDto {

    @field:JsonProperty("message")
    @get:JsonProperty("message")
    override val message: String = "Cannot create indexation"

    @field:JsonProperty("type")
    @get:JsonProperty("type")
    override val type: ExceptionTypeDto = ExceptionTypeDto.CANNOT_CREATE_INDEXATION_EXCEPTION
}

@JsonTypeName("PRODUCT_NOT_FOUND_EXCEPTION")
data class ProductNotFoundExceptionDto @JsonCreator constructor(

    @field:JsonProperty("productName")
    @get:JsonProperty("productName")
    @param:JsonProperty("productName")
    val productName: String,

    @field:JsonProperty("requestId")
    @get:JsonProperty("requestId")
    @param:JsonProperty("requestId")
    override val requestId: String,

) : ExceptionDto {

    @field:JsonProperty("message")
    @get:JsonProperty("message")
    override val message: String = "Cannot found product '$productName'"

    @field:JsonProperty("type")
    @get:JsonProperty("type")
    override val type: ExceptionTypeDto = ExceptionTypeDto.PRODUCT_NOT_FOUND_EXCEPTION
}

@JsonTypeName("NOT_AVAILABLE_EXCEPTION")
data class NotAvailableOperationExceptionDto @JsonCreator constructor(

    @field:JsonProperty("userLogin")
    @get:JsonProperty("userLogin")
    @param:JsonProperty("userLogin")
    val userLogin: String,

    @field:JsonProperty("requestId")
    @get:JsonProperty("requestId")
    @param:JsonProperty("requestId")
    override val requestId: String,
) : ExceptionDto {

    @field:JsonProperty("message")
    @get:JsonProperty("message")
    override val message: String = "This operation is not available for user $userLogin"

    @field:JsonProperty("type")
    @get:JsonProperty("type")
    override val type: ExceptionTypeDto = ExceptionTypeDto.NOT_AVAILABLE_OPERATION_EXCEPTION
}

@JsonTypeName("UNKNOWN_EXCEPTION")
data class UnknownExceptionDto @JsonCreator constructor(

    @field:JsonProperty("requestId")
    @get:JsonProperty("requestId")
    @param:JsonProperty("requestId")
    override val requestId: String,
) : ExceptionDto {

    @field:JsonProperty("type")
    @get:JsonProperty("type")
    override val type: ExceptionTypeDto = ExceptionTypeDto.UNKNOWN_EXCEPTION

    @field:JsonProperty("type")
    @get:JsonProperty("type")
    override val message: String = "Unknown server exception"
}
