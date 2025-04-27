create table indexation_task
(
    indexation_id                bigint      not null,
    last_indexed_entity_ts       bigint      not null,
    runner_name                  varchar(64) not null,
    entities_with_indexed_ts_cnt int         not null default 0,
    active_status                varchar(16) not null,

    primary key (indexation_id, runner_name)
);

