package nda.search.general.web.api.impl.mapper

import nda.search.general.api.dto.indexation.IndexationCreateRqDto
import nda.search.general.api.dto.indexation.IndexationEntityTypeDto
import nda.search.general.api.dto.indexation.IndexationInfoDto
import nda.search.general.domain.model.IndexationEntityType
import nda.search.general.domain.model.IndexationInfo
import nda.search.general.domain.model.requests.IndexationCreateRq

object IndexationInfoMapper {
    fun map(model: IndexationInfo): IndexationInfoDto = IndexationInfoDto(
        indexationId = model.indexationId,
        lastIndexedEntityTs = model.lastIndexedEntityTs,
        indexationEntityType = IndexationEntityTypeDto.valueOf(model.indexationEntityType.name),
        enabled = model.enabled,
    )

    fun map(dto: IndexationCreateRqDto): IndexationCreateRq = IndexationCreateRq(
        startedTs = dto.startedTs,
        indexationEntityType = IndexationEntityType.valueOf(dto.indexationEntityType.name),
    )
}
