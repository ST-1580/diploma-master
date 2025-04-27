package nda.search.general.domain.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import nda.search.general.domain.db.Tables.RAG_SETTINGS
import nda.search.general.domain.db.tables.records.RagSettingsRecord
import nda.search.general.domain.model.ActivityStatus
import nda.search.general.domain.model.RagSettings
import nda.search.general.domain.repository.RagSettingsRepository

@Repository
class RagSettingsRepositoryImpl(
    private val dslContext: DSLContext,
) : RagSettingsRepository {
    override fun getActiveIndexSettings(): List<RagSettings> {
        return dslContext
            .selectFrom(RAG_SETTINGS)
            .where(RAG_SETTINGS.STATUS.eq(ActivityStatus.ACTIVE.name))
            .fetch(::toModel)
    }

    companion object {
        private fun toModel(ragSettingsRecord: RagSettingsRecord): RagSettings {
            return with(ragSettingsRecord) {
                RagSettings(
                    id = get(RAG_SETTINGS.ID),
                    ragIndexId = get(RAG_SETTINGS.RAG_INDEX_ID),
                    filterId = get(RAG_SETTINGS.FILTER_ID),
                    configId = get(RAG_SETTINGS.CONFIG_ID),
                )
            }
        }
    }
}
