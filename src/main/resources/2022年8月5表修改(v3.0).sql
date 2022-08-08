## 部署拆借的视图至线上
CREATE TABLE budget_reimbursementorder_lack_bill (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  reimbursementid bigint(20) DEFAULT NULL COMMENT '报销单id',
  bunitid bigint(20) DEFAULT NULL COMMENT '开票单位id',
  bunitname varchar(255) NOT NULL COMMENT '开票单位名称',
  project varchar(255) NOT NULL COMMENT '开票项目',
  money decimal(18,4) NOT NULL DEFAULT '0.0000' COMMENT '金额',
  estimated_return_time date NOT NULL COMMENT '预计还票时间',
  bill_status tinyint(1) NOT NULL COMMENT '状态 （0：未签收 1：已签收）',
  yearid bigint(20) NOT NULL COMMENT '届别id',
  monthid bigint(20) NOT NULL COMMENT '月份id',
  create_time datetime NOT NULL COMMENT '创建时间',
  create_by varchar(100) DEFAULT NULL COMMENT '创建人',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  update_by varchar(100) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  KEY lack_bill_ibfk_1 (reimbursementid) USING BTREE,
  KEY lack_bill_ibfk_2 (bunitid, money,estimated_return_time) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='预算欠票表';


ALTER TABLE budget_reimbursementorder ADD lack_bill bit(1) DEFAULT b'0' COMMENT '是否欠票（0：否 1：是）';
ALTER TABLE budget_year_agentadd ADD is_exempt_fine bit(1) DEFAULT b'0' COMMENT '是否免罚 0 否 1是';
ALTER TABLE budget_year_agentadd ADD exempt_fine_reason varchar(500) DEFAULT NULL COMMENT '免罚原因';
ALTER TABLE budget_year_agentadd ADD exempt_result bit(1) DEFAULT NULL COMMENT '免罚结果 0 免罚 1 罚款';
ALTER TABLE budget_year_agentadd ADD fine_remark varchar(500) DEFAULT NULL COMMENT '罚款理由说明';


alter table budget_unit add column budget_responsibilities varchar(255) NULL COMMENT '预算责任人工号，多个以逗号分隔';
alter table budget_year_agentlend add column `is_cross_dept` bit(1) DEFAULT b'0' COMMENT '是否跨部门';
update budget_year_agentlend set is_cross_dept = 1 where inunitid != outunitid


CREATE TABLE `budget_year_agentlend_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `year_agent_lend_id` bigint(20) NOT NULL COMMENT '拆借主表id',
  `inunitid` bigint(20) NOT NULL COMMENT '拆进预算单位id',
  `insubjectname` varchar(255) DEFAULT NULL COMMENT '拆进科目名',
  `insubjectid` bigint(20) NOT NULL COMMENT '拆进科目id',
  `inyearagentid` bigint(20) DEFAULT NULL COMMENT '拆进动因id',
  `inname` varchar(255) DEFAULT NULL COMMENT '拆进动因名称',
  `outunitid` bigint(20) DEFAULT NULL COMMENT '拆出预算单位id',
  `outsubjectname` varchar(255) DEFAULT NULL COMMENT '拆出科目名',
  `outsubjectid` bigint(11) DEFAULT NULL COMMENT '拆出科目id',
  `outyearagentid` bigint(20) DEFAULT NULL COMMENT '拆出动因id',
  `outname` varchar(255) DEFAULT NULL COMMENT '拆出动因名称',
  `total` decimal(18,4) DEFAULT '0.0000' COMMENT '拆进金额',
  `remark` varchar(1000) DEFAULT NULL COMMENT '拆借原因',
  `requeststatus` int(1) DEFAULT NULL COMMENT '拆借状态（冗余方便报表统计，改动最小），-1：退回，0：保存，1：已提交（待审核），2：审核通过',
  `outagentmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前动因年度预算金额（年初预算）',
  `outagentaddmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前动因累计追加金额',
  `outagentlendoutmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前拆出金额（同科目里面的动因可以拆借）',
  `outagentlendinmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前拆进金额（同科目里面的动因可以拆借）',
  `outagentexcutemoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前动因累计执行金额',
  `inagentmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前动因年度预算金额（年初预算',
  `inagentaddmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前动因累计追加金额',
  `inagentlendoutmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前拆出金额（同科目里面的动因可以拆借）',
  `inagentlendinmoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前拆进金额（同科目里面的动因可以拆借）',
  `inagentexcutemoney` decimal(50,4) NOT NULL DEFAULT '0.0000' COMMENT '拆借前动因累计执行金额',
  `is_exempt_fine` bit(1) DEFAULT b'0' COMMENT '是否免罚 0 否 1是',
  `exempt_fine_reason` varchar(500) DEFAULT NULL COMMENT '免罚原因',
  `exempt_fine_result` varchar(500) DEFAULT NULL COMMENT '免罚结果',
  `fine_reason_remark` varchar(500) DEFAULT NULL COMMENT '罚款理由说明',
  `audittime` datetime DEFAULT NULL COMMENT '审批时间（冗余方便报表统计，改动最小）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='年度动因拆借明细';

