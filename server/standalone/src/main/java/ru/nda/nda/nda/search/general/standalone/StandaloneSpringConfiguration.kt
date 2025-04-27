package nda.search.general.standalone

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import nda.search.general.web.WebSpringConfiguration
import ru.nda.library.spring.boot.security.starter.paths.configurer.AuthorizeRequestsPathConfigurer

@Configuration
@Import(WebSpringConfiguration::class)
class StandaloneSpringConfiguration {
    @Bean
    fun authorizeRequestsPathConfigurer(): AuthorizeRequestsPathConfigurer {
        return AuthorizeRequestsPathConfigurer { registry ->
            registry.requestMatchers("/ping").permitAll()
            // safe because actuator uses port 8081, not connected with balancer
            registry.requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
        }
    }
}
