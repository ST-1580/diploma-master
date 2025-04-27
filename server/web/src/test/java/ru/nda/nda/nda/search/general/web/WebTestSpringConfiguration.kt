package nda.search.general.web

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import nda.search.general.application.ApplicationTestSpringConfiguration

@SpringBootConfiguration
@ComponentScan(
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [WebSpringConfiguration::class],
        ),
    ],
)
@Import(
    ApplicationTestSpringConfiguration::class,
)
@PropertySource("classpath:web-testing.properties")
class WebTestSpringConfiguration
