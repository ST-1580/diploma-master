package nda.search.general.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import nda.search.general.api.dto.indexation.IndexationCreateRqDto
import nda.search.general.api.dto.indexation.IndexationEntityTypeDto
import nda.search.general.api.dto.indexation.IndexationInfoDto
import ru.nda.library.spring.boot.jackson.mapper.starter.DefaultObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExceptionMappingTest : WebTest {
    private val mapper: ObjectMapper = DefaultObjectMapper()
    private val client = HttpClient.newHttpClient()

    @LocalServerPort
    private var port = 0

    @Test
    fun `test ping`() {
        val rq = HttpRequest.newBuilder(URI.create("http://localhost:$port/ping"))
            .GET()
            .build()
        val rs = client.send(rq, BodyHandlers.ofString())
        assertEquals(200, rs.statusCode())
        assertEquals("pong", rs.body())
    }

    @Test
    fun `test ok response`() {
        val expected = IndexationInfoDto(
            indexationId = 0,
            lastIndexedEntityTs = 100L,
            indexationEntityType = IndexationEntityTypeDto.BLOCK,
            enabled = false,
        )

        val (code, body) = createIndexationInfo(
            body = IndexationCreateRqDto(
                expected.lastIndexedEntityTs,
                expected.indexationEntityType,
            )
        ) {
            assertNotNull(it)
            mapper.treeToValue(it, IndexationInfoDto::class.java)
        }
        assertEquals(HttpStatus.OK.value(), code)

        assertThat(body)
            .usingRecursiveComparison()
            .ignoringFields("indexationId")
            .ignoringFieldsOfTypes(Instant::class.java)
            .isEqualTo(expected)
    }

    private fun <R> createIndexationInfo(body: IndexationCreateRqDto, responseProcessor: (JsonNode?) -> R): Pair<Int, R> {
        val request = HttpRequest
            .newBuilder(
                URI.create("http://localhost:$port/api/indexation"),
            )
            .header("Content-type", MediaType.APPLICATION_JSON_VALUE)
            .POST(
                body
                    .let(mapper::writeValueAsBytes)
                    .let(BodyPublishers::ofByteArray)
            )
            .build()

        val response = client.send(
            request,
            BodyHandlers.ofByteArray(),
        )

        return response.statusCode() to
            response.body().let(mapper::readTree).let(responseProcessor)
    }
}
