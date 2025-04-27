create table if not exists indexed_knowledges(
    id bigserial primary key,
    rag_index_id bigint not null,
    knowledge_id bigint not null,
    produced_doc_id varchar(64) not null,
    status varchar(32) not null,
    updated_ts bigint not null
);

alter table indexed_knowledges add constraint udx__indexed_knowledges__rag_id_knowledge_id_produced_doc unique (rag_index_id, knowledge_id, produced_doc_id);

create index idx__indexed_knowledges__rag_id_knowledge_id_status on indexed_knowledges(rag_index_id, knowledge_id, status);
