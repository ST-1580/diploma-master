package nda.search.general.application.indexation.tp.pools

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import nda.search.general.application.indexation.IndexationConstants
import ru.nda.library.spring.boot.task.processing.core.starter.tp.TpPool

@Component
class EntitiesFromNdaReceiverTpPool @Autowired constructor(
    @Value("\${nda.search.general.indexer.pool.size:4}") override val threadPoolSize: Int,
    @Value("\${nda.search.general.indexer.enabled:true}") override val enabled: Boolean,
) : TpPool {
    override val name = POOL_NAME

    companion object {
        const val POOL_NAME = IndexationConstants.PoolName.NDA_INDEXING_POOL
    }
}
