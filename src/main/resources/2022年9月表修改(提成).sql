alter table budget_paybatch add pay_template_type  int(1) not null comment '付款模板类型 1:招行批量付款 2:招行代发付款 3:老模板' DEFAULT 3;
CREATE TABLE `budget_extract_account_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `extract_code` varchar(255) DEFAULT NULL COMMENT '提成单号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `plan_accountant_emp_nos` varchar(255) DEFAULT NULL COMMENT '计划做账会计',
  `billing_unit_id` bigint(20) DEFAULT NULL COMMENT '发放单位',
  `accountant_emp_no` varchar(20) DEFAULT NULL COMMENT '实际做账人(工号)',
  `accountant_time` datetime DEFAULT NULL COMMENT '做账完成时间',
  `accountant_status` int(1) DEFAULT NULL COMMENT '做账状态。0：未完成 1：已完成',
  `extract_month` varchar(255) NOT NULL COMMENT '提成批次',
  `task_type` int(1) DEFAULT NULL COMMENT '类型。1：提成支付申请单 3：延期支付申请单',
  `delay_extract_code` varchar(255) DEFAULT NULL COMMENT '延期提成单号',
  `batch` int(5) DEFAULT NULL COMMENT '批次',
  `personality_ids` varchar(255) DEFAULT NULL COMMENT '员工个体户id列表',
  `is_should_account` bit(1) DEFAULT b'1' COMMENT '（延期任务时需要使用，留存记录）是否需要做账',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成会计做账';


CREATE TABLE `budget_extract_per_pay_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `extract_month` varchar(20) DEFAULT NULL COMMENT '提成批次',
  `extract_code` varchar(255) DEFAULT NULL COMMENT '提成单号',
  `billing_unit_id` bigint(20) DEFAULT NULL COMMENT '付款单位',
  `billing_unit_account` varchar(50) DEFAULT NULL COMMENT '付款单位账户',
  `billing_unit_branch_code` varchar(255) DEFAULT NULL COMMENT '付款单位账户电子银联号',
  `billing_unit_bank_name` varchar(255) DEFAULT NULL COMMENT '付款单位银行类型',
  `billing_unit_open_bank` varchar(255) DEFAULT NULL COMMENT '付款单位开户行',
  `billing_unit_name` varchar(255) DEFAULT NULL COMMENT '付款单位名称',
  `pay_money` decimal(18,2) DEFAULT NULL COMMENT '发放金额',
  `is_company_emp` bit(1) DEFAULT NULL COMMENT '是否是公司员工',
  `personality_id` bigint(20) DEFAULT NULL COMMENT '员工个体户id',
  `receiver_code` varchar(50) DEFAULT NULL COMMENT '收款人标识',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收款人名称',
  `receiver_account_name` varchar(255) DEFAULT NULL COMMENT '收款人户名',
  `receiver_bank_account` varchar(50) DEFAULT NULL COMMENT '收款人银行账号',
  `receiver_bank_account_branch_code` varchar(50) DEFAULT NULL COMMENT '收款人银行账号电子银联号',
  `receive_bank_account_bank_name` varchar(255) DEFAULT NULL COMMENT '收款人银行类型',
  `receiver_open_bank` varchar(255) DEFAULT NULL COMMENT '收款人开户行',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `pay_status` int(1) DEFAULT '1' COMMENT '发放状态 1：正常 2：调账 3：延期',
  PRIMARY KEY (`id`),
  KEY `extract_month` (`extract_month`,`extract_code`,`receiver_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成每笔发放明细表';


CREATE TABLE `budget_extract_personality_pay_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `personality_id` bigint(20) DEFAULT NULL COMMENT '员工个体户id',
  `extract_month` varchar(20) DEFAULT NULL COMMENT '提成批次',
  `receipt_sum` decimal(18,2) DEFAULT NULL COMMENT '累计交票',
  `extract_sum` decimal(18,2) DEFAULT NULL COMMENT '累计已发提成',
  `cur_extract` decimal(18,2) DEFAULT NULL COMMENT '当期待发放提成',
  `cur_real_extract` decimal(18,2) DEFAULT NULL COMMENT '当期提成发放金额',
  `salary_sum` decimal(18,2) DEFAULT NULL COMMENT '累计已发工资',
  `cur_salary` decimal(18,2) DEFAULT NULL COMMENT '当期工资发放金额',
  `welfare_sum` decimal(18,2) DEFAULT NULL COMMENT '累计已发福利',
  `cur_welfare` decimal(18,2) DEFAULT NULL COMMENT '当期福利发放金额',
  `remaining_invoices` decimal(18,2) DEFAULT NULL COMMENT '剩余票额',
  `remaining_pay_limit_money` decimal(18,2) DEFAULT NULL COMMENT '剩余发放限额',
  `billing_unit_id` bigint(20) DEFAULT NULL COMMENT '发放公司id',
  `pay_status` int(1) DEFAULT NULL COMMENT '发放状态 1：正常 2：调账 3：延期',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `operate_time` datetime DEFAULT NULL COMMENT '确认完成/确认发放时间',
  `is_send` bit(1) DEFAULT NULL COMMENT '是否发放 1：是',
  `is_init_data` bit(1) DEFAULT b'0' COMMENT '是否是初始化数据',
  PRIMARY KEY (`id`),
  UNIQUE KEY `personality_id` (`extract_month`,`personality_id`,`billing_unit_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成员工个体户发放明细';

CREATE TABLE `budget_extract_tax_handle_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `extract_month` varchar(20) DEFAULT NULL COMMENT '提成批次',
  `is_cal_complete` bit(1) DEFAULT NULL COMMENT '是否计算完成',
  `is_set_excess_complete` bit(1) DEFAULT NULL COMMENT '是否设置超额完成',
  `is_personality_complete` bit(1) DEFAULT NULL COMMENT '员工个体户是否设置完成',
  PRIMARY KEY (`id`),
  UNIQUE KEY `extract_month` (`extract_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成税务处理状态表';

CREATE TABLE `budget_extractpayment_outer_unit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `extract_month` varchar(20) DEFAULT NULL COMMENT '提成批次',
  `extract_payment_id` bigint(20) DEFAULT NULL COMMENT '提成发放id',
  `billing_unit_id` bigint(20) DEFAULT NULL COMMENT '发放单位id',
  `unit_bank_account` varchar(255) DEFAULT NULL COMMENT '发放单位账户',
  `branchcode` varchar(255) DEFAULT NULL COMMENT '电子联行号',
  `bank_name` varchar(255) DEFAULT NULL COMMENT '银行类型',
  `open_bank` varchar(255) DEFAULT NULL COMMENT '开户行',
  `pay_money` decimal(18,2) DEFAULT NULL COMMENT '发放金额',
  `billing_unit_name` varchar(255) DEFAULT NULL COMMENT '发放单位名称',
  PRIMARY KEY (`id`),
  KEY `extract_payment_id` (`extract_payment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非员工个体户的外部单位发放明细';



# --李子耀 sql
-- db_budget.budget_extract_commission_application definition

CREATE TABLE `budget_extract_commission_application` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
 `extract_sum_id` bigint(20) DEFAULT NULL COMMENT '提成id主表',
 `status` int(11) DEFAULT NULL COMMENT '-2 作废（退回才可以作废）,-1(退回，仍可以修改。可以作废)  ，0 草稿（撤回）,1已提交,2审核通过',
 `department_no` varchar(100) DEFAULT NULL COMMENT '部门',
 `department_name` varchar(20) DEFAULT NULL COMMENT '部门名称',
 `payment_reason` varchar(256) DEFAULT NULL COMMENT '支付事由 支付+“届别”+“月份”+“批次”+“提成/坏账”\r\n届别取“届别”字段；月份取“提成期间”中的月份；“提成/坏账”根据“坏账（是/否）”判断，若是则显示“坏账”；否则显示“提成”。',
 `remarks` varchar(256) DEFAULT NULL COMMENT '备注',
 `annual_quota` int(11) DEFAULT NULL COMMENT '年额度',
 `create_by` varchar(20) DEFAULT NULL COMMENT '创建人',
 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
 `update_by` varchar(20) DEFAULT NULL COMMENT '更新人',
 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
 `reimbursement_id` bigint(20) DEFAULT NULL COMMENT '报销单id',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COMMENT='提成支付申请单  主表 ';

-- db_budget.budget_extract_commission_application_budget_details definition

CREATE TABLE `budget_extract_commission_application_budget_details` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`application_id` bigint(20) NOT NULL COMMENT '申请单id',
`subject_code` varchar(20) DEFAULT NULL COMMENT '科目编码',
`subject_name` varchar(20) DEFAULT NULL COMMENT '科目名称',
`budget_amount` decimal(10,2) DEFAULT NULL COMMENT '金额 根据提成类型+届别取提成明细所在行的“申请提成”',
`create_by` varchar(20) DEFAULT NULL COMMENT '创建人',
`create_time` datetime DEFAULT NULL COMMENT '创建时间',
`update_by` varchar(20) DEFAULT NULL COMMENT '更新人',
`update_time` datetime DEFAULT NULL COMMENT '更新时间',
`motivation_name` varchar(200) DEFAULT NULL COMMENT '动因名称',
`motivation_id` bigint(20) DEFAULT NULL COMMENT '动因id',
`subject_id` bigint(20) DEFAULT NULL COMMENT '科目id',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COMMENT='提成支付申请单  附表 预算明细';


-- db_budget.budget_extract_commission_application_log definition

CREATE TABLE `budget_extract_commission_application_log` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`application_id` bigint(20) NOT NULL COMMENT '申请单id',
`status` int(4) NOT NULL COMMENT '操作状态  1 同意 2拒绝',
`node` int(20) NOT NULL COMMENT '操作节点    枚举',
`remarks` varchar(20) DEFAULT NULL COMMENT '备注 操作信息',
`create_by` varchar(20) DEFAULT NULL COMMENT '操作人',
`create_time` datetime DEFAULT NULL COMMENT '操作时间',
`creator_name` varchar(20) DEFAULT NULL COMMENT '创建人名称',
`request_id` varchar(50) DEFAULT NULL COMMENT 'oa流程id',
`request_code` varchar(50) DEFAULT NULL COMMENT 'OA流程编码',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COMMENT='申请单 oa 审批日志记录';

-- db_budget.budget_individual_employee_files definition

CREATE TABLE `budget_individual_employee_files` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`employee_job_num` int(11) NOT NULL COMMENT '员工工号',
`phone` varchar(20) DEFAULT NULL COMMENT '联系电话\r\n',
`account_type` int(11) DEFAULT NULL COMMENT '账户类型  1个卡 2 公户',
`account_name` varchar(20) DEFAULT NULL COMMENT '户名\r\n',
`deposit_bank` varchar(100) DEFAULT NULL COMMENT '开户行',
`issued_unit` varchar(100) DEFAULT NULL COMMENT '发放单位',
`release_opinions` varchar(100) DEFAULT NULL COMMENT '发放意见',
`social_security_stop_date` datetime DEFAULT NULL COMMENT '社保停发日期',
`leave_date` datetime DEFAULT NULL COMMENT '离职日期',
`service_agreement` varchar(100) DEFAULT NULL COMMENT '服务协议',
`self_or_agency` varchar(10) DEFAULT NULL COMMENT '自办还是代办  1自办 2 代办',
`platform_company` varchar(100) DEFAULT NULL COMMENT '平台公司',
`verification_audit` varchar(100) DEFAULT NULL COMMENT '核定/查账',
`annual_quota` decimal(10,2) DEFAULT NULL COMMENT '年额度',
`create_by` varchar(20) DEFAULT NULL COMMENT '创建人',
`create_time` datetime DEFAULT NULL COMMENT '创建时间',
`update_by` varchar(20) DEFAULT NULL COMMENT '更新人',
`update_time` datetime DEFAULT NULL COMMENT '更新时间',
`status` int(11) DEFAULT '1' COMMENT '状态 1 正常  2停用',
`batch_no` varchar(100) DEFAULT NULL COMMENT '批次',
`department_no` varchar(100) DEFAULT NULL COMMENT '部门',
`department_name` varchar(100) DEFAULT NULL COMMENT '部门名称',
`province_or_region` varchar(100) DEFAULT NULL COMMENT '省区/大区',
`employee_name` varchar(100) DEFAULT NULL COMMENT '员工名称',
`account` varchar(20) DEFAULT NULL COMMENT '账号',
`remarks` varchar(256) DEFAULT NULL,
PRIMARY KEY (`id`),
UNIQUE KEY `individual_employee_files_unique_index` (`employee_job_num`,`account_name`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COMMENT='员工个体户档案';

-- db_budget.budget_individual_employee_ticket_receipt definition

CREATE TABLE `budget_individual_employee_ticket_receipt` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`ticket_code` varchar(50) NOT NULL COMMENT '收票单号',
`employee_job_num` int(11) NOT NULL COMMENT '员工工号',
`individual_employee_info_id` bigint(20) NOT NULL COMMENT '员工档案id',
`individual_name` varchar(100) DEFAULT NULL COMMENT '个体户名称',
`invoice_amount` decimal(10,2) DEFAULT NULL COMMENT '发票总金额',
`remarks` varchar(256) DEFAULT NULL COMMENT '备注',
`create_by` varchar(20) DEFAULT NULL COMMENT '创建人',
`create_time` datetime DEFAULT NULL COMMENT '创建时间',
`update_by` varchar(20) DEFAULT NULL COMMENT '更新人',
`update_time` datetime DEFAULT NULL COMMENT '更新时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COMMENT='员工个体户收票信息主表 维护档案';
-- db_budget.budget_individual_employee_ticket_receipt_info definition

CREATE TABLE `budget_individual_employee_ticket_receipt_info` (
`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`employee_job_num` int(11) NOT NULL COMMENT '员工工号',
`individual_employee_info_id` int(11) NOT NULL COMMENT '员工档案id',
`individual_name` varchar(100) DEFAULT NULL COMMENT '个体户名称',
`year` int(11) DEFAULT NULL COMMENT '年份',
`month` int(11) DEFAULT NULL COMMENT '月份',
`invoice_amount` decimal(10,2) DEFAULT NULL COMMENT '发票金额',
`remarks` varchar(256) DEFAULT NULL COMMENT '备注',
`create_by` varchar(20) DEFAULT NULL COMMENT '创建人',
`create_time` datetime DEFAULT NULL COMMENT '创建时间',
`update_by` varchar(20) DEFAULT NULL COMMENT '更新人',
`update_time` datetime DEFAULT NULL COMMENT '更新时间',
`ticket_id` bigint(20) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COMMENT='员工个体户收票信息维护档案';


-- db_budget.budget_common_attachment definition

CREATE TABLE `budget_common_attachment` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`contact_id` bigint(20) NOT NULL COMMENT '关联id',
`file_type` int(4) DEFAULT NULL,
`file_url` varchar(100) DEFAULT NULL,
`file_ext_name` varchar(10) DEFAULT NULL,
`file_name` varchar(100) DEFAULT NULL,
`create_time` datetime DEFAULT NULL,
`creator` varchar(100) DEFAULT NULL,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4;



# 部分表字段新增
