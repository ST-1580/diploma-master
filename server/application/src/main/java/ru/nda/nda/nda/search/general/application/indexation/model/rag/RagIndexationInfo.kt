package nda.search.general.application.indexation.model.rag

data class RagIndexationInfo(
    val canSwitch: Boolean = false,
    val indexedDocsCnt: Int = 0,
    val indexVersion: Long = -1L,
)
