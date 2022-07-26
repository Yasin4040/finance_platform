alter table budget_year_agentaddinfo add is_exempt_fine bit default 0 comment '是否免罚 0 否 1是';
alter table budget_year_agentaddinfo add exempt_fine_reason varchar(500) default null comment '免罚原因';

alter table budget_year_agentlend add is_exempt_fine bit default 0 comment '是否免罚 0 否 1是';
alter table budget_year_agentlend add exempt_fine_reason varchar(500) default null comment '免罚原因';