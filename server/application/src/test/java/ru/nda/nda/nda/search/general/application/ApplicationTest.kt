package nda.search.general.application

import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    classes = [
        ApplicationTestSpringConfiguration::class,
    ],
)
interface ApplicationTest
