create table rag_index_to_indexation_info(
    indexation_info_id bigint not null,
    rag_index_id bigint not null,
    status varchar(16) not null,

     primary key (indexation_info_id, rag_index_id)
);