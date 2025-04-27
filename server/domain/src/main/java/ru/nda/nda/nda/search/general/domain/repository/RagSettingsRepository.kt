package nda.search.general.domain.repository

import nda.search.general.domain.model.RagSettings

interface RagSettingsRepository {
    fun getActiveIndexSettings(): List<RagSettings>
}
