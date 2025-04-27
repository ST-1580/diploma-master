package nda.search.general.application

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import nda.search.general.domain.DomainSpringConfiguration

@Configuration
@ComponentScan
@Import(DomainSpringConfiguration::class)
class ApplicationSpringConfiguration
