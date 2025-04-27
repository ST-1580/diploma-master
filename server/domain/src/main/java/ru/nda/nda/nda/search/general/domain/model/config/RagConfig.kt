package nda.search.general.domain.model.config

sealed interface RagConfig {
    val type: ConfigType
    val configId: Long
}
