package nda.search.general.standalone.health

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object HealthIndicatorPools {
    val default: ScheduledExecutorService = Executors.newScheduledThreadPool(4)
}
