package nda.search.general.domain.model.config

data class SimpleConfig<T>(
    override val type: ConfigType,
    override val configId: Long,
    val value: T,
) : RagConfig
