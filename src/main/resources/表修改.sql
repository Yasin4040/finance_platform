-- 添加报销详情科目主键
alter table budget_reimbursementorder_detail add subjectid bigint null comment '科目主键'
update budget_reimbursementorder_detail de set subjectid = (select sid from(
select d.id as did,d.subjectname,o.yearid,o.unitid as ounitid,tt.sid
from 
budget_reimbursementorder o,budget_reimbursementorder_detail d,
(select s.yearid,us.unitid,s.id as sid,s.name from budget_unit_subject us,budget_subject s where s.id = us.subjectid) tt
where o.id = d.reimbursementid
and tt.yearid = o.yearid
and tt.unitid = o.unitid
and tt.name = d.subjectname) temp
where temp.did = de.id)
-- 脏数据设置为-1
update budget_reimbursementorder_detail de set subjectid = -1 where subjectid is null
-- 修改为必填项
alter table budget_reimbursementorder_detail modify subjectid bigint not null comment '科目主键'

-- 添加报销划拨科目主键
alter table budget_reimbursementorder_allocated add subjectid bigint null comment '科目主键'
update budget_reimbursementorder_allocated de set subjectid = (select sid from(
select d.id as did,d.subjectname,o.yearid,o.unitid as ounitid,tt.sid
from 
budget_reimbursementorder o,budget_reimbursementorder_allocated d,
(select s.yearid,us.unitid,s.id as sid,s.name from budget_unit_subject us,budget_subject s where s.id = us.subjectid) tt
where o.id = d.reimbursementid
and tt.yearid = o.yearid
and tt.unitid = o.unitid
and tt.name = d.subjectname) temp
where temp.did = de.id)
-- 脏数据设置为-1
update budget_reimbursementorder_allocated de set subjectid = -1 where subjectid is null
-- 修改为必填项
alter table budget_reimbursementorder_allocated modify subjectid bigint not null comment '科目主键'

alter table budget_reimbursementorder add financialmanagereceivestatus bit(1) null comment '财务总监审核接收状态 1:已接收 0：未接收';
alter table budget_reimbursementorder add financialmanagetime varchar(255) null comment '财务总监审核时间';
alter table budget_reimbursementorder add financialmanagestatus bit(1) null comment '财务总监审核状态 0：未审核；1：已审核';
alter table budget_reimbursementorder add financialmanager varchar(255) null comment '财务总监（工号，登录账号）';
alter table budget_reimbursementorder add generalmanagereceivestatus bit(1) null comment '总经理审核接收状态 1:已接收 0：未接收';
alter table budget_reimbursementorder add generalmanagetime varchar(255) null comment '总经理审核时间';
alter table budget_reimbursementorder add generalmanagestatus bit(1) null comment '总经理审核状态 0：未审核；1：已审核';
alter table budget_reimbursementorder add generalmanager varchar(255) null comment '总经理（工号，登录账号）';


-- 划拨单添加预算单位
alter table budget_reimbursementorder_allocated add unitid bigint null comment '预算单位主键'
-- 初始化
update budget_reimbursementorder_allocated m inner join (select 
u.id as unitid,
t.id
from budget_reimbursementorder_allocated t,budget_unit u,budget_reimbursementorder o
where 
t.reimbursementid = o.id
and t.unitname = u.name
and o.yearid = u.yearid
) temp
on m.id = temp.id set m.unitid = temp.unitid
-- 修改为必填项
alter table budget_reimbursementorder_allocated modify unitid bigint not null comment '预算单位主键'

-- 报销单添加审核环节和条件版本字段
alter table budget_reimbursementorder add work_flow_step  varchar(255) null comment '审核流程'
alter table budget_reimbursementorder add work_flow_version  int(11) null comment '审核流程条件版本'

-- 年度、月度预算追加info表预算科目Id非必填
alter table budget_year_agentaddinfo modify subjectid bigint null comment '预算科目id';
alter table budget_month_agentaddinfo modify subjectid bigint null comment '预算科目id';

-- 月度预算追加total默认值为0
alter table budget_month_agentaddinfo alter column total set default 0;

-- 添加流程条件
DROP TABLE IF EXISTS `tab_flow_condition`;
CREATE TABLE `tab_flow_condition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_type` varchar(2) NOT NULL COMMENT '流程类型：1：报销  2：其他',
  `step_dm` varchar(25) NOT NULL COMMENT '环节代码',
  `condition_step_dm` varchar(25) NOT NULL COMMENT '条件环节代码',
  `the_condition` char(1) NOT NULL COMMENT '条件 报销条件（1：已接收 2：审核通过） 其他自定义',
  `the_version` int(11) NOT NULL COMMENT '版本',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tab_link_limit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `procedure_id` bigint(20) NOT NULL COMMENT '流程id',
  `subject_id` bigint(20) NOT NULL COMMENT '预算科目id',
  `link_dm` varchar(200) NOT NULL COMMENT '环节代码',
  `min_limit` decimal(18,4) NOT NULL DEFAULT '0.0000' COMMENT '最小限度',
  `max_limit` decimal(18,4) DEFAULT NULL COMMENT '最大限度',
  `is_active` char(1) NOT NULL COMMENT '是否启用 0否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=275 DEFAULT CHARSET=utf8mb4 COMMENT='环节限制表';

CREATE TABLE `tab_procedure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `yearid` bigint(20) NOT NULL COMMENT '届别主键',
  `procedure_name` varchar(100) NOT NULL COMMENT '流程名称',
  `procedure_type` char(1) NOT NULL COMMENT '流程类型 1报销 2其他',
  `procedure_link_order` varchar(200) NOT NULL COMMENT '环节顺序',
  `is_active` char(1) NOT NULL COMMENT '是否启用 0否 1是',
  `is_delete` char(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COMMENT='流程表';