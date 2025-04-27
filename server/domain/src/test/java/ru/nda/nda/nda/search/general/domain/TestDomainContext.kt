package nda.search.general.domain

import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertNotNull

class TestDomainContext : DomainTest {

    @Autowired
    private lateinit var dslContext: DSLContext

    @Test
    fun testContext() {
        assertNotNull(dslContext)
    }
}
