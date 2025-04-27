package nda.search.general.application

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import nda.search.general.domain.DomainTestSpringConfiguration

@SpringBootConfiguration
@ComponentScan(
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [ApplicationSpringConfiguration::class],
        ),
    ],
)
@Import(
    DomainTestSpringConfiguration::class,
)
@PropertySource("classpath:application-testing.properties")
class ApplicationTestSpringConfiguration
