alter table indexation_task add column task_uid varchar(64);

alter table indexation_task add constraint udx__indexation_task__task_uid unique (task_uid);