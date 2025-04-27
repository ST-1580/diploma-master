package nda.search.general.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import nda.search.general.application.integration.nda.EntitiesFromNdaReceiverService
import nda.search.general.application.integration.conversion.YfmConverterService
import nda.search.general.application.integration.rag.RagAnswerService
import nda.search.general.application.integration.rag.RagIndexationService
import ru.nda.library.spring.boot.feign.auth.tvm.starter.interceptor.factory.FeignAuthTvmInterceptorFactory
import ru.nda.library.spring.boot.feign.client.commons.interceptor.FeignAuthInterceptor
import ru.nda.library.spring.boot.feign.client.starter.factory.FeignClientFactory

@Configuration
class IntegrationSpringConfiguration {

    @Bean
    fun ndaIndexationIntegration(
        clientFactory: FeignClientFactory,
        authTvmInterceptor: FeignAuthTvmInterceptorFactory,
        @Value("\${api.url.nda.api}") ndaUrl: String,
        @Value("\${api.tvm.alias.nda.api}") ndaTvmAlias: String,
    ): EntitiesFromNdaReceiverService {
        return clientFactory.createSpringAnnotatedClient(
            ndaUrl,
            authTvmInterceptor.create(ndaTvmAlias),
            EntitiesFromNdaReceiverService::class.java
        )
    }

    @Bean
    fun ragIndexationIntegration(
        clientFactory: FeignClientFactory,
        authTvmInterceptor: FeignAuthTvmInterceptorFactory,
        @Value("\${api.url.rag.api}") ragUrl: String,
        @Value("\${api.tvm.alias.rag.api}") ragTvmAlias: String,
    ): RagIndexationService {
        return clientFactory.createSpringAnnotatedClient(
            ragUrl,
            authTvmInterceptor.create(ragTvmAlias),
            RagIndexationService::class.java
        )
    }

    @Bean
    fun ragAnswerIntegration(
        clientFactory: FeignClientFactory,
        authTvmInterceptor: FeignAuthTvmInterceptorFactory,
        @Value("\${api.url.rag.answer.api}") ragAnswerUrl: String,
        @Value("\${api.tvm.alias.rag.api}") ragAnswerTvmAlias: String,
    ): RagAnswerService {
        return clientFactory.createSpringAnnotatedClient(
            ragAnswerUrl,
            authTvmInterceptor.create(ragAnswerTvmAlias),
            RagAnswerService::class.java
        )
    }

    @Bean
    fun yfmConverterIntegration(
        clientFactory: FeignClientFactory,
        @Value("\${api.url.yfm.converter.api}") yfmConverterUrl: String,
    ): YfmConverterService {
        return clientFactory.createSpringAnnotatedClient(
            yfmConverterUrl,
            FeignAuthInterceptor.NO_AUTH,
            YfmConverterService::class.java
        )
    }
}
