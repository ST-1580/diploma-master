package nda.search.general.web

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [
        WebTestSpringConfiguration::class,
    ],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
interface WebTest
