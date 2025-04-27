package nda.search.general.api

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import nda.search.general.api.dto.ResponseStatusDto
import nda.search.general.api.dto.hints.RagHintsRequestDto
import nda.search.general.api.dto.hints.RagHintsResponseDto
import nda.search.general.api.dto.hints.score.RagHintScoreRequestDto

@RestController
@RequestMapping(
    path = [
        "/api/hints",
    ],
)
interface RagHintsController {

    @PostMapping
    fun getHints(@RequestBody ragHintsRequestDto: RagHintsRequestDto): RagHintsResponseDto

    @PostMapping("/score")
    fun score(@RequestBody ragHintScoreDto: RagHintScoreRequestDto): ResponseStatusDto
}
