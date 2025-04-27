package nda.search.general.domain.model

data class RagIndex(
    val id: Long,
    val serviceName: String,
    val productName: String,
    val indexName: String?,
    val ndaClusterIds: List<Long> = emptyList(),
)
