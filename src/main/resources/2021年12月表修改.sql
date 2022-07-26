--银行账户修改日志表
DROP TABLE IF EXISTS `invoke_record`;
CREATE TABLE `invoke_record`  (
                                  `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                  `bank_id` bigint(20) NULL DEFAULT NULL COMMENT '银行账户编号',
                                  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                  `creator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人姓名',
                                  `create_oid` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人编号',
                                  `pre_body` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改前数据',
                                  `post_body` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改后数据',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

--退单类型表
DROP TABLE IF EXISTS `charge_back_type`;
CREATE TABLE `charge_back_type`  (
                                     `id` int(11) NOT NULL,
                                     `back_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '返单类型',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of charge_back_type
-- ----------------------------
INSERT INTO `charge_back_type` VALUES (1, '发票错误');
INSERT INTO `charge_back_type` VALUES (2, '审核未签字');
INSERT INTO `charge_back_type` VALUES (3, '附件不全');
INSERT INTO `charge_back_type` VALUES (4, '月份错误');
INSERT INTO `charge_back_type` VALUES (5, '科目错误');
INSERT INTO `charge_back_type` VALUES (6, '动因错误');
INSERT INTO `charge_back_type` VALUES (7, '面单错误');
INSERT INTO `charge_back_type` VALUES (8, '开票单位错误');
INSERT INTO `charge_back_type` VALUES (9, '金额错误');
INSERT INTO `charge_back_type` VALUES (10, '超时报销');
INSERT INTO `charge_back_type` VALUES (11, '其他');

SET FOREIGN_KEY_CHECKS = 1;


alter table budget_extract_importdetail add extract_type  varchar(255) not null comment '提成类型(2021-12月新增)';
alter table budget_extract_importdetail add should_send_extract  DECIMAL(18,2) not null comment '应发提成(2021-12月新增)';
alter table budget_extract_importdetail add tax  decimal(18,2) null comment '个税(2021-12月新增)';
alter table budget_extract_importdetail add tax_reduction  decimal(18,2)  null comment '个税减免(2021-12月新增)';
alter table budget_extract_importdetail add invoice_excess_tax decimal(18,2)  null comment '发票超额税金(2021-12月新增)';
alter table budget_extract_importdetail add invoice_excess_tax_reduction  decimal(18,2)  null comment '票超额税金减免(2021-12月新增)';
alter table budget_extractpayment add before_cal_fee  DECIMAL(18,2) null comment '费用发放(计算之前)';

DROP TABLE IF EXISTS `budget_extract_qrcode`;
CREATE TABLE `budget_extract_qrcode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `extract_month` varchar(255) NOT NULL COMMENT '提成批次',
  `qrcode` longtext NOT NULL COMMENT '二维码 base64字符',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成批次二维码';

DROP TABLE IF EXISTS `budget_extract_fee_pay_detail`;
CREATE TABLE `budget_extract_fee_pay_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `extract_month` varchar(255) NOT NULL COMMENT '提成批次',
  `empno` varchar(50) NOT NULL COMMENT '工号',
  `empname` varchar(255) NOT NULL COMMENT '名称',
  `fee_pay` decimal(18,2) DEFAULT NULL COMMENT '费用发放',
  `creatorname` varchar(255) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成费用发放明细';

DROP TABLE IF EXISTS `budget_extract_sign_log`;
CREATE TABLE `budget_extract_sign_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `extract_month` varchar(255) NOT NULL COMMENT '提成批次',
  `empno` varchar(20) NOT NULL COMMENT '接收人工号',
  `empname` varchar(255) NOT NULL COMMENT '姓名',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提成签收日志';



