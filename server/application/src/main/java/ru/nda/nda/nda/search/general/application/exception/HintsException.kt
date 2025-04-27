package nda.search.general.application.exception

sealed class HintsException(
    message: String? = null,
    cause: Throwable? = null,
    enableSuppression: Boolean = true,
    writableStackTrace: Boolean = true,
) : RuntimeException(message, cause, enableSuppression, writableStackTrace) {

    class ProductNotFoundException(
        val productName: String,
        message: String? = "Not found product '$productName'",
    ) : HintsException(message)
}
