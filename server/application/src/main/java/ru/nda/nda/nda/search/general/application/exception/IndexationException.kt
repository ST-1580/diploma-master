package nda.search.general.application.exception

sealed class IndexationException(
    message: String? = null,
    cause: Throwable? = null,
    enableSuppression: Boolean = true,
    writableStackTrace: Boolean = true,
) : RuntimeException(message, cause, enableSuppression, writableStackTrace) {

    class IndexationNotFoundException(
        val indexationId: Long,
        message: String? = "Not found indexation with id = '$indexationId'",
    ) : IndexationException(message)

    class CannotCreateIndexationException(
        message: String? = "Cannot create indexation",
    ) : IndexationException(message)

    class NotAvailableOperationException(
        val userLogin: String,
        message: String? = "This operation is not available for user $userLogin",
    ) : IndexationException(message)
}
