package nda.search.general.web.exception.mapping

import org.springframework.http.HttpStatus
import nda.search.general.api.dto.exception.CannotCreateIndexationExceptionDto
import nda.search.general.api.dto.exception.IndexationNotFoundExceptionDto
import nda.search.general.api.dto.exception.NotAvailableOperationExceptionDto
import nda.search.general.application.exception.IndexationException
import ru.nda.library.spring.boot.security.model.current.RequestContext

object IndexationExceptionToDtoMapper {
    fun exceptionToResponse(exception: IndexationException) = when (exception) {
        is IndexationException.IndexationNotFoundException -> map(exception)
        is IndexationException.CannotCreateIndexationException -> map(exception)
        is IndexationException.NotAvailableOperationException -> map(exception)
    }

    private fun map(exception: IndexationException.IndexationNotFoundException) = ExceptionMapper.ExceptionResponse(
        HttpStatus.BAD_REQUEST,
        IndexationNotFoundExceptionDto(
            exception.indexationId,
            requestId(),
        ),
    )

    private fun map(exception: IndexationException.CannotCreateIndexationException) = ExceptionMapper.ExceptionResponse(
        HttpStatus.BAD_REQUEST,
        CannotCreateIndexationExceptionDto(
            requestId(),
        ),
    )

    private fun map(exception: IndexationException.NotAvailableOperationException) = ExceptionMapper.ExceptionResponse(
        HttpStatus.FORBIDDEN,
        NotAvailableOperationExceptionDto(
            exception.userLogin,
            requestId(),
        ),
    )

    private fun requestId(): String = RequestContext.current()?.requestId ?: "unknown"
}
