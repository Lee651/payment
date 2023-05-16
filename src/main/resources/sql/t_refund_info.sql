CREATE TABLE `t_refund_info` (
                                 `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '退款单id',
                                 `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
                                 `refund_no` varchar(50) DEFAULT NULL COMMENT '商户退款单编号',
                                 `refund_id` varchar(50) DEFAULT NULL COMMENT '支付系统退款单号',
                                 `total_fee` int(11) DEFAULT NULL COMMENT '原订单金额(分)',
                                 `refund` int(11) DEFAULT NULL COMMENT '退款金额(分)',
                                 `reason` varchar(50) DEFAULT NULL COMMENT '退款原因',
                                 `refund_status` varchar(10) DEFAULT NULL COMMENT '退款状态',
                                 `content_return` text COMMENT '申请退款返回参数',
                                 `content_notify` text COMMENT '退款结果通知参数',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;
