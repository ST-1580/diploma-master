create table rag_index
(
    id           bigint       not null primary key ,
    product_name varchar(256) not null unique
);

create table rag_index_to_nda_cluster(
    rag_index_id bigint not null ,
    nda_cluster_id bigint not null ,

    primary key (rag_index_id, nda_cluster_id)
)