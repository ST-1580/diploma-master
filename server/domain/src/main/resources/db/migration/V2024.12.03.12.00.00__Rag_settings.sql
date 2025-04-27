create table rag_settings(
    id bigserial primary key,
    rag_index_id bigint not null,
    filter_id bigint not null,
    config_id bigint not null,
    status varchar(16) not null
);

create index idx__rag_settings__status on rag_settings (status);

create table filter_property(
    id bigserial primary key,
    filter_id bigint not null,
    type varchar(64) not null,
    value jsonb not null,
    status varchar(16) not null
);

create index idx__filter_property__filter_id_type on filter_property (filter_id, type) where status = 'ACTIVE';

create table config_property(
    id bigserial primary key,
    config_id bigint not null,
    type varchar(64) not null,
    value jsonb not null,
    status varchar(16) not null
);

create index idx__config_property__config_id_type on config_property (config_id, type) where status = 'ACTIVE';