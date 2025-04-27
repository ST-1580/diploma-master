create table indexation_info
(
    id                     bigserial primary key,
    last_indexed_entity_ts bigint      not null,
    entity_type            varchar(32) not null,
    enabled                boolean     not null default false
);