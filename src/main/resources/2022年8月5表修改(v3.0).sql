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

