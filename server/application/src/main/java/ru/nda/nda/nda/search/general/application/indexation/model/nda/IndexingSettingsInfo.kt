package nda.search.general.application.indexation.model.nda

import nda.search.general.domain.model.RagSettings

data class IndexingSettingsInfo(
    val knowledgeIdsBySettingsId: Map<Long, List<Long>>,
    val ragSettingsBySettingsId: Map<Long, RagSettings>,
    val outsideFilterKnowledgeIdsBySettingId: Map<Long, List<Long>>,
)
