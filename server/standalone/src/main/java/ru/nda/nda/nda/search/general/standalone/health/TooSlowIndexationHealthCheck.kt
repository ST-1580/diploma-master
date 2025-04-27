package nda.search.general.standalone.health

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import nda.search.general.application.indexation.IndexationService
import ru.nda.library.spring.boot.telemetry.actuator.starter.health.indicator.AbstractAsyncHealthIndicator
import java.time.Clock
import kotlin.time.Duration.Companion.seconds

@Component
@ConditionalOnProperty(
    prefix = "health.check.too.slow.indexation",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class TooSlowIndexationHealthCheck(
    private val indexationService: IndexationService,
    @Value("\${health.check.too.slow.indexation.unchecked.delay.seconds:1800}")
    private val uncheckedDelaySeconds: Long,
    @Value("\${health.check.too.slow.indexation.timeout.seconds:300}")
    timeout: Long,
) : AbstractAsyncHealthIndicator(
        pool = HealthIndicatorPools.default,
        timeout = timeout.seconds,
    ) {
    override fun doHealthCheck(): Health {
        val indexationInfos = indexationService.getAllActive()

        val currentTime = Clock.systemUTC().millis()
        val timeForCheck = currentTime - uncheckedDelaySeconds * 1000

        LOG.debug("Want to check that all runners more than $timeForCheck updatedTs")

        val badIndexations = indexationInfos.filter { it.lastIndexedEntityTs < timeForCheck }
        if (badIndexations.isEmpty()) {
            return Health.up().build()
        }

        return Health.down().withDetail(
            SLOW_INDEXATION_IDS,
            badIndexations.map { it.indexationId }
        ).build()
    }

    companion object {
        val LOG = LoggerFactory.getLogger(TooSlowIndexationHealthCheck::class.java)

        private const val SLOW_INDEXATION_IDS = "slowIndexationIds"
    }
}
