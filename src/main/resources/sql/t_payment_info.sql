CREATE TABLE `t_payment_info` (
                                  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '支付记录id',
                                  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
                                  `transaction_id` varchar(50) DEFAULT NULL COMMENT '支付系统交易编号',
                                  `payment_type` varchar(20) DEFAULT NULL COMMENT '支付类型',
                                  `trade_type` varchar(20) DEFAULT NULL COMMENT '交易类型',
                                  `trade_state` varchar(50) DEFAULT NULL COMMENT '交易状态',
                                  `payer_total` int(11) DEFAULT NULL COMMENT '支付金额(分)',
                                  `content` text COMMENT '通知参数',
                                  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4;
