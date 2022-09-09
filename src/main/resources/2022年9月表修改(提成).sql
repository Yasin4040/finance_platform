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