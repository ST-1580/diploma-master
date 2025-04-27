package nda.search.general.domain

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import ru.nda.library.spring.boot.application.parent.starter.ApplicationParentTestConfiguration

@SpringBootConfiguration
@ComponentScan(
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [DomainSpringConfiguration::class],
        ),
    ],
)
@Import(
    ApplicationParentTestConfiguration::class,
)
@PropertySource("classpath:domain-testing.properties")
class DomainTestSpringConfiguration
