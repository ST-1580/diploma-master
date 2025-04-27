package nda.search.general.domain.repository

import nda.search.general.domain.model.config.RagConfig

interface ConfigRepository {
    fun getConfigsById(configIds: Collection<Long>): List<RagConfig>
}
