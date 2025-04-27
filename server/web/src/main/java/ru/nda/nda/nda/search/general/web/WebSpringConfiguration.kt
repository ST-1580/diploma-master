package nda.search.general.web

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import nda.search.general.application.ApplicationSpringConfiguration

@Configuration
@Import(ApplicationSpringConfiguration::class)
@ComponentScan
class WebSpringConfiguration
