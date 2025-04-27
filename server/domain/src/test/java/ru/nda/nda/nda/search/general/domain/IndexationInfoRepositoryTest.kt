package nda.search.general.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import nda.search.general.domain.model.IndexationEntityType
import nda.search.general.domain.model.IndexationInfo
import nda.search.general.domain.model.requests.IndexationCreateRq
import nda.search.general.domain.repository.IndexationInfoRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class IndexationInfoRepositoryTest : DomainTest {

    @Autowired
    private lateinit var indexationInfoRepository: IndexationInfoRepository

    @Test
    fun `test create and get`() {
        val indexationCreateRq = IndexationCreateRq(
            startedTs = 100L,
            indexationEntityType = IndexationEntityType.BLOCK,
        )
        val expected = IndexationInfo(
            indexationId = 0L,
            lastIndexedEntityTs = indexationCreateRq.startedTs,
            indexationEntityType = indexationCreateRq.indexationEntityType,
            enabled = false
        )

        val created = indexationInfoRepository.create(indexationCreateRq)
        assertNotNull(created)
        assertThat(created)
            .usingRecursiveComparison()
            .ignoringFields("indexationId")
            .isEqualTo(expected)

        val loaded = indexationInfoRepository.get(created.indexationId)
        assertEquals(created, loaded)
    }

    @Test
    fun `test update`() {
        val indexationCreateRq = IndexationCreateRq(
            startedTs = 100L,
            indexationEntityType = IndexationEntityType.BLOCK,
        )
        val expected = IndexationInfo(
            indexationId = 0L,
            lastIndexedEntityTs = indexationCreateRq.startedTs,
            indexationEntityType = indexationCreateRq.indexationEntityType,
            enabled = true
        )

        val created = indexationInfoRepository.create(indexationCreateRq)
        assertNotNull(created)
        assertFalse(created.enabled)

        val updated = indexationInfoRepository.updateIndexationEnable(created.indexationId, true)
        assertThat(updated)
            .usingRecursiveComparison()
            .ignoringFields("indexationId")
            .isEqualTo(expected)
    }
}
