package nda.search.general.web.exception.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver
import nda.search.general.api.dto.exception.ExceptionDto
import nda.search.general.api.dto.exception.UnknownExceptionDto
import nda.search.general.application.exception.HintsException
import nda.search.general.application.exception.IndexationException
import ru.nda.library.spring.boot.security.model.current.RequestContext

@Component
class ExceptionMapper @Autowired constructor(
    mapper: ObjectMapper,
) : AbstractHandlerExceptionResolver() {
    private val objectWriter = mapper.writerFor(ExceptionDto::class.java)

    override fun doResolveException(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?,
        ex: Exception,
    ): ModelAndView {
        val exceptionResponse = resolveRecursively(ex, ex)

        writeResponse(exceptionResponse, response)
        return ModelAndView()
    }

    private fun resolveRecursively(root: Exception, ex: Exception,): ExceptionResponse {
        val cause = ex.cause
        return when {
            ex is IndexationException -> {
                IndexationExceptionToDtoMapper.exceptionToResponse(ex).also {
                    LOG.debug(
                        "Mapped IndexationException to response of type {} and status code {}",
                        it.body.type,
                        it.status,
                        ex,
                    )
                }
            }
            ex is HintsException -> {
                HintsExceptionToDtoMapper.exceptionToResponse(ex).also {
                    LOG.debug(
                        "Mapped HintsException to response of type {} and status code {}",
                        it.body.type,
                        it.status,
                        ex,
                    )
                }
            }
            cause != null && cause is Exception -> {
                return resolveRecursively(root, cause)
            }
            else -> unknownExceptionToResponse()
        }
    }

    private fun unknownExceptionToResponse(): ExceptionResponse = ExceptionResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        UnknownExceptionDto(
            requestId = RequestContext.current()?.requestId ?: "unknown",
        ),
    )

    private fun writeResponse(exceptionResponse: ExceptionResponse, response: HttpServletResponse,) {
        response.status = exceptionResponse.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE

        val body = objectWriter.writeValueAsString(exceptionResponse.body)

        response.writer.write(body)
        response.writer.flush()
    }

    class ExceptionResponse(
        val status: HttpStatus,
        val body: ExceptionDto,
    )

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ExceptionMapper::class.java)
    }
}
