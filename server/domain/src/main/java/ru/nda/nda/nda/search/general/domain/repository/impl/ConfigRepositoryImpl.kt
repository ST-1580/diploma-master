package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.CONFIG_PROPERTY
import nda.search.general.domain.db.tables.records.ConfigPropertyRecord
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.config.ConfigType
import nda.search.general.domain.model.config.RagConfig
import nda.search.general.domain.model.config.SimpleConfig
import nda.search.general.domain.repository.ConfigRepository

@Repository
class ConfigRepositoryImpl(
    private val dslContext: DSLContext,
) : ConfigRepository {
    override fun getConfigsById(configIds: Collection<Long>): List<RagConfig> {
        return dslContext
            .selectFrom(CONFIG_PROPERTY)
            .where(CONFIG_PROPERTY.CONFIG_ID.`in`(configIds).and(CONFIG_PROPERTY.STATUS.eq(ActivityStatus.ACTIVE.name)))
            .fetch(::toModel)
    }

    companion object {
        private fun toModel(configRecord: ConfigPropertyRecord): RagConfig {
            return with(configRecord) {
                val configType = ConfigType.valueOf(get(CONFIG_PROPERTY.TYPE))
                when (configType) {
                    ConfigType.CONFIG_CLASS_NAME -> {
                        SimpleConfig<String>(
                            type = configType,
                            configId = get(CONFIG_PROPERTY.CONFIG_ID),
                            value = get(CONFIG_PROPERTY.VALUE).data(),
                        )
                    }
                }
            }
        }
    }
}
