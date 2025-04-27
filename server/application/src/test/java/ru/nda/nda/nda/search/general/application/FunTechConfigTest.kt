package nda.search.general.application

import org.junit.jupiter.api.Test
import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityForIndexBatchDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockEmbeddedEntityTypeDto
import ru.nda.api.integration.searchgeneral.dto.index.BlockTranslationForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeForIndexDto
import ru.nda.api.integration.searchgeneral.dto.index.KnowledgeWithBlocksForIndexDto
import ru.nda.api.v1.dto.BlockTypeDto
import ru.nda.api.v1.dto.ChannelTypeDto
import ru.nda.api.v1.dto.EntityStatusDto
import nda.search.general.application.indexation.configs.FunTechIndexationConfig
import kotlin.test.assertEquals

class FunTechConfigTest {

    @Test
    fun `check fun tech config with solution and ml`() {
        val patternRusId = "1234"
        val patternRusId2 = "5678"
        val knowledgeId = 322L
        val knowledgeTitle = "Это тайтл знания"

        val embeddedEntityRus = BlockEmbeddedEntityForIndexDto(
            text = "Я русский текст",
            title = "Привет",
            type = BlockEmbeddedEntityTypeDto.SAMSARA_PATTERN,
            id = patternRusId,
            startPosition = 25,
            length = 6,
        )

        val block = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.SOLUTION,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.ML,
        )

        val block2 = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.SOLUTION,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.CHAT,
        )

        val block3 = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.PROBLEM,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.ML,
        )

        val knowledge = KnowledgeWithBlocksForIndexDto(
            knowledge = KnowledgeForIndexDto(
                clusterId = 1,
                sectionId = 1,
                knowledgeId = knowledgeId,
                tagIds = emptySet(),
                title = knowledgeTitle,
                status = EntityStatusDto.ACTIVE,
                triggeredUpdatedTs = 1,
            ),
            blocks = listOf(block, block2, block3),
        )

        val ragDocs = FunTechIndexationConfig.getIndexBatch(listOf(knowledge))

        assertEquals(1, ragDocs.size)

        assertEquals("$knowledgeId", ragDocs[0].docId)
        assertEquals(
            expected = "Это тайтл знания\n" +
                "\n" +
                "Решение ML\n" +
                "\n" +
                "### Проблема\nтекст текст <template><template_title>Привет</template_title>Я русский текст</template>",
            actual = ragDocs[0].text
        )
    }

    @Test
    fun `check fun tech config with solution and chat`() {
        val patternRusId = "1234"
        val patternRusId2 = "5678"
        val knowledgeId = 322L
        val knowledgeTitle = "Это тайтл знания"

        val embeddedEntityRus = BlockEmbeddedEntityForIndexDto(
            text = "Я русский текст",
            title = "Привет",
            type = BlockEmbeddedEntityTypeDto.SAMSARA_PATTERN,
            id = patternRusId,
            startPosition = 25,
            length = 6,
        )

        val block2 = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.SOLUTION,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.CHAT,
        )

        val block3 = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.PROBLEM,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.ML,
        )

        val knowledge = KnowledgeWithBlocksForIndexDto(
            knowledge = KnowledgeForIndexDto(
                clusterId = 1,
                sectionId = 1,
                knowledgeId = knowledgeId,
                tagIds = emptySet(),
                title = knowledgeTitle,
                status = EntityStatusDto.ACTIVE,
                triggeredUpdatedTs = 1,
            ),
            blocks = listOf(block2, block3),
        )

        val ragDocs = FunTechIndexationConfig.getIndexBatch(listOf(knowledge))

        assertEquals(1, ragDocs.size)

        assertEquals("$knowledgeId", ragDocs[0].docId)
        assertEquals(
            expected = "Это тайтл знания\n" +
                "\n" +
                "Решение Чаты\n" +
                "\n" +
                "### Проблема\nтекст текст <template><template_title>Привет</template_title>Я русский текст</template>",
            actual = ragDocs[0].text
        )
    }

    @Test
    fun `check fun tech config with problem and ml`() {
        val patternRusId = "1234"
        val patternRusId2 = "5678"
        val knowledgeId = 322L
        val knowledgeTitle = "Это тайтл знания"

        val embeddedEntityRus = BlockEmbeddedEntityForIndexDto(
            text = "Я русский текст",
            title = "Привет",
            type = BlockEmbeddedEntityTypeDto.SAMSARA_PATTERN,
            id = patternRusId,
            startPosition = 25,
            length = 6,
        )

        val block3 = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.PROBLEM,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.ML,
        )

        val knowledge = KnowledgeWithBlocksForIndexDto(
            knowledge = KnowledgeForIndexDto(
                clusterId = 1,
                sectionId = 1,
                knowledgeId = knowledgeId,
                tagIds = emptySet(),
                title = knowledgeTitle,
                status = EntityStatusDto.ACTIVE,
                triggeredUpdatedTs = 1,
            ),
            blocks = listOf(block3),
        )

        val ragDocs = FunTechIndexationConfig.getIndexBatch(listOf(knowledge))

        assertEquals(1, ragDocs.size)

        assertEquals("$knowledgeId", ragDocs[0].docId)
        assertEquals(
            expected = "Это тайтл знания\n" +
                "\n" +
                "Проблема ML\n" +
                "\n" +
                "### Проблема\nтекст текст <template><template_title>Привет</template_title>Я русский текст</template>",
            actual = ragDocs[0].text
        )
    }

    @Test
    fun `check fun tech config when have i in title`() {
        val patternRusId = "1234"
        val patternRusId2 = "5678"
        val knowledgeId = 322L
        val knowledgeTitle = "[i] Это тайтл знания"

        val embeddedEntityRus = BlockEmbeddedEntityForIndexDto(
            text = "Я русский текст",
            title = "Привет",
            type = BlockEmbeddedEntityTypeDto.SAMSARA_PATTERN,
            id = patternRusId,
            startPosition = 25,
            length = 6,
        )

        val block3 = BlockTranslationForIndexDto(
            text = "### Проблема\nтекст текст <уйду>",
            catalogId = 1,
            sectionId = 1,
            clusterId = 1,
            knowledgeId = knowledgeId,
            id = 100,
            tagIds = emptySet(),
            embeddedEntities = BlockEmbeddedEntityForIndexBatchDto(batch = listOf(embeddedEntityRus)),
            triggeredUpdatedTs = 1,
            blockType = BlockTypeDto.PROBLEM,
            status = EntityStatusDto.ACTIVE,
            channelType = ChannelTypeDto.ML,
        )

        val knowledge = KnowledgeWithBlocksForIndexDto(
            knowledge = KnowledgeForIndexDto(
                clusterId = 1,
                sectionId = 1,
                knowledgeId = knowledgeId,
                tagIds = emptySet(),
                title = knowledgeTitle,
                status = EntityStatusDto.ACTIVE,
                triggeredUpdatedTs = 1,
            ),
            blocks = listOf(block3),
        )

        val ragDocs = FunTechIndexationConfig.getIndexBatch(listOf(knowledge))

        assertEquals(0, ragDocs.size)
    }
}
