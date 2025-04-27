package nda.search.general.web.api.impl

import org.springframework.stereotype.Service
import nda.search.general.api.RagHintsController
import nda.search.general.api.dto.ResponseStatusDto
import nda.search.general.api.dto.hints.RagHintsRequestDto
import nda.search.general.api.dto.hints.RagHintsResponseDto
import nda.search.general.api.dto.hints.score.RagHintScoreRequestDto
import nda.search.general.application.indexation.RagService
import nda.search.general.web.api.auth.UserService
import nda.search.general.web.api.impl.mapper.RagHintsMapper

@Service
class RagHintsControllerImpl(
    private val ragService: RagService,

    private val userService: UserService,
) : RagHintsController {

    override fun getHints(ragHintsRequestDto: RagHintsRequestDto): RagHintsResponseDto {
        userService.checkUser()

        val answerRq = RagHintsMapper.map(ragHintsRequestDto)
        val answerWithRequest = ragService.getHints(answerRq)
        return RagHintsMapper.map(
            requestId = answerWithRequest.first,
            response = answerWithRequest.second
        )
    }

    override fun score(ragHintScoreDto: RagHintScoreRequestDto): ResponseStatusDto {
        userService.checkUser()

        val scoreRq = RagHintsMapper.map(ragHintScoreDto)
        ragService.score(scoreRq)

        return ResponseStatusDto.OK
    }
}
