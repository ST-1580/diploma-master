package nda.search.general.application.indexation

internal object IndexationConstants {

    object PoolName {
        const val NDA_INDEXING_POOL = "EntitiesFromNdaReceiverTpPool"
    }

    object NdaRunner {
        const val ALL_KNOWLEDGES = "AllKnowledgesRunner"

        const val BLOCKS_FROM_BLOCK_TRANSLATION_VERSION = "BlocksFromNdaBlockTranslationVersionRunner"
        const val BLOCKS_FROM_EMBEDDED_ENTITIES = "BlocksFromNdaByEmbeddedEntitiesRunner"
        const val BLOCKS_FROM_KNOWLEDGE_BLOCK_LINK = "BlocksFromNdaByKnowledgeBlockLinkRunner"

        const val KNOWLEDGES_FROM_KNOWLEDGE_PROPERTY = "KnowledgesFromNdaByKnowledgePropertyRunner"
        const val KNOWLEDGES_FROM_KNOWLEDGE = "KnowledgesFromNdaByKnowledgeRunner"
    }
}
