package nda.search.general.standalone

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import nda.search.general.web.WebTestSpringConfiguration

@SpringBootConfiguration
@ComponentScan(
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [
                StandaloneSpringConfiguration::class,
                Application::class,
            ],
        ),
    ],
)
@Import(
    WebTestSpringConfiguration::class,
)
@PropertySource("classpath:standalone-testing.properties")
class StandaloneTestSpringConfiguration
