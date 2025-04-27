package nda.search.general.application.indexation

import org.springframework.stereotype.Service
import nda.search.general.application.indexation.configs.DefaultIndexationConfig
import nda.search.general.application.indexation.configs.FunTechIndexationConfig
import nda.search.general.application.indexation.configs.HtmlIndexationConfig
import nda.search.general.application.indexation.configs.IndexationConfigService
import nda.search.general.application.indexation.configs.SimpleIndexationConfig
import nda.search.general.application.indexation.configs.TravelIndexationConfig
import nda.search.general.domain.model.config.ConfigClassName
import nda.search.general.domain.model.config.ConfigType
import nda.search.general.domain.model.config.RagConfig
import nda.search.general.domain.model.config.SimpleConfig
import nda.search.general.domain.repository.ConfigRepository

@Service
class ConfigService(
    private val configRepository: ConfigRepository,
    private val htmlIndexationConfig: HtmlIndexationConfig,
) {
    fun getConfigsByConfigId(configIds: List<Long>): Map<Long, List<RagConfig>> {
        return configRepository.getConfigsById(configIds).groupBy { it.configId }
    }

    fun getConfigClassByConfigId(configsByConfigId: Map<Long, List<RagConfig>>): Map<Long, IndexationConfigService> {
        return configsByConfigId.mapValues {
            val classNameConfig =
                it.value.firstOrNull { config -> config.type == ConfigType.CONFIG_CLASS_NAME } as SimpleConfig<*>
            val className = classNameConfig.value

            return@mapValues when (className) {
                is String -> {
                    when (className) {
                        ConfigClassName.TRAVEL_CONFIG -> TravelIndexationConfig
                        ConfigClassName.FUN_TECH_CONFIG -> FunTechIndexationConfig
                        ConfigClassName.SIMPLE_CONFIG -> SimpleIndexationConfig
                        ConfigClassName.HTML_CONFIG -> htmlIndexationConfig
                        else -> DefaultIndexationConfig
                    }
                }
                else -> DefaultIndexationConfig
            }
        }
    }
}
