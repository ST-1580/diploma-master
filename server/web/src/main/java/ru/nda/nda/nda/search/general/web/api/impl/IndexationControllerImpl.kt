package nda.search.general.web.api.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController
import nda.search.general.api.IndexationController
import nda.search.general.api.dto.ResponseStatusDto
import nda.search.general.api.dto.indexation.IndexationCreateRqDto
import nda.search.general.api.dto.indexation.IndexationInfoDto
import nda.search.general.application.indexation.IndexationService
import nda.search.general.application.indexation.RagService
import nda.search.general.web.api.auth.UserService
import nda.search.general.web.api.impl.mapper.IndexationInfoMapper

@RestController
class IndexationControllerImpl @Autowired constructor(
    private val indexationService: IndexationService,
    private val ragService: RagService,
    private val userService: UserService,
) : IndexationController {

    override fun create(indexationCreateRqDto: IndexationCreateRqDto): IndexationInfoDto {
        userService.checkUser()

        return indexationService
            .create(IndexationInfoMapper.map(indexationCreateRqDto))
            .let(IndexationInfoMapper::map)
    }

    override fun get(indexationId: Long): IndexationInfoDto {
        userService.checkUser()

        return indexationService
            .get(indexationId)
            .let(IndexationInfoMapper::map)
    }

    override fun startIndexation(indexationId: Long): IndexationInfoDto {
        userService.checkUser()

        return indexationService
            .updateIndexationEnable(indexationId, true)
            .let(IndexationInfoMapper::map)
    }

    override fun stopIndexation(indexationId: Long): IndexationInfoDto {
        userService.checkUser()

        return indexationService
            .updateIndexationEnable(indexationId, false)
            .let(IndexationInfoMapper::map)
    }

    override fun updateIndexationRunners(indexationId: Long): IndexationInfoDto {
        userService.checkUser()

        return indexationService
            .updateIndexationRunners(indexationId)
            .let(IndexationInfoMapper::map)
    }

    override fun initRagIndex(ragIndexId: Long): ResponseStatusDto {
        userService.checkUser()

        ragService.initRagIndex(ragIndexId)

        return ResponseStatusDto.OK
    }
}
