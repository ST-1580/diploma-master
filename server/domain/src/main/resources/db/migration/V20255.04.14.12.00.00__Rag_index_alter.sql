alter table rag_index add column if not exists service_name varchar(64) not null default 'nda';

alter table rag_index add column if not exists index_name varchar(64) default null;
