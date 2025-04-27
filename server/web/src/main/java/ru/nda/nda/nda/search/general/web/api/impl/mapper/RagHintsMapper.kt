package nda.search.general.web.api.impl.mapper

import nda.search.general.api.dto.hints.AnswerDto
import nda.search.general.api.dto.hints.DialogMessageDto
import nda.search.general.api.dto.hints.RagHintsRequestDto
import nda.search.general.api.dto.hints.RagHintsResponseDto
import nda.search.general.api.dto.hints.score.RagHintScoreRequestDto
import nda.search.general.api.dto.hints.score.ScoreDto
import nda.search.general.application.indexation.dto.rag.answer.RagAnswerRpDto
import nda.search.general.application.indexation.dto.rag.answer.RagAnswerRqDto
import nda.search.general.application.indexation.dto.rag.answer.RagDialogDto
import nda.search.general.application.indexation.dto.rag.answer.score.RagScoreDto
import nda.search.general.application.indexation.dto.rag.answer.score.RagScoreRqDto

object RagHintsMapper {
    fun map(rq: RagHintsRequestDto): RagAnswerRqDto {
        return RagAnswerRqDto(
            service = NONE,
            product = rq.product,
            indexName = NONE,
            replies = rq.replies,
            dialog = rq.dialog.map(::map),
            metaFeatures = rq.metaFeatures,
            options = rq.options,
        )
    }

    fun map(rq: RagHintScoreRequestDto): RagScoreRqDto {
        return RagScoreRqDto(
            service = NONE,
            product = rq.product,
            dialog = rq.dialog.map(::map),
            metaFeatures = rq.metaFeatures,
            reply = rq.reply,
            score = map(rq.score),
            ticketId = rq.ticketId,
            scoredRequestId = rq.scoredRequestId,
            prompt = rq.prompt,
        )
    }

    fun map(dialog: DialogMessageDto): RagDialogDto {
        return RagDialogDto(
            text = dialog.text,
            role = dialog.role,
            id = dialog.id,
            created = dialog.created,
        )
    }

    fun map(scoreDto: ScoreDto): RagScoreDto {
        return RagScoreDto(
            status = scoreDto.status,
            relevance = scoreDto.relevance,
            modifiedText = scoreDto.modifiedText,
            comment = scoreDto.comment,
        )
    }

    fun map(requestId: String, response: RagAnswerRpDto): RagHintsResponseDto {
        return RagHintsResponseDto(
            answers = response.answers.map {
                AnswerDto(
                    answer = it.answer,
                    meta = it.meta,
                )
            },
            requestId = requestId
        )
    }

    const val NONE = "NONE"
}
