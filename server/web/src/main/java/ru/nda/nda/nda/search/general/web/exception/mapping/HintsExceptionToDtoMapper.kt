package nda.search.general.web.exception.mapping

import org.springframework.http.HttpStatus
import nda.search.general.api.dto.exception.ProductNotFoundExceptionDto
import nda.search.general.application.exception.HintsException
import ru.nda.library.spring.boot.security.model.current.RequestContext

object HintsExceptionToDtoMapper {
    fun exceptionToResponse(exception: HintsException) = when (exception) {
        is HintsException.ProductNotFoundException -> map(exception)
    }

    private fun map(exception: HintsException.ProductNotFoundException) = ExceptionMapper.ExceptionResponse(
        HttpStatus.BAD_REQUEST,
        ProductNotFoundExceptionDto(
            exception.productName,
            requestId(),
        ),
    )

    private fun requestId(): String = RequestContext.current()?.requestId ?: "unknown"
}
