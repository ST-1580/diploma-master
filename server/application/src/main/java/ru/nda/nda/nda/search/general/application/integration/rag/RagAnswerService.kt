package nda.search.general.application.integration.rag

import feign.Response
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import nda.search.general.application.indexation.dto.rag.answer.RagAnswerRqDto
import nda.search.general.application.indexation.dto.rag.answer.score.RagScoreRqDto

@RestController()
@RequestMapping(
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE]
)
interface RagAnswerService {

    @PostMapping("/answer")
    fun answer(rq: RagAnswerRqDto): Response

    @PostMapping("/score")
    fun score(rq: RagScoreRqDto)
}
