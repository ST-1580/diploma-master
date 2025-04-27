package nda.search.general.domain.model.requests

import nda.search.general.domain.model.IndexationEntityType

data class IndexationCreateRq(
    val startedTs: Long,
    val indexationEntityType: IndexationEntityType,
)
