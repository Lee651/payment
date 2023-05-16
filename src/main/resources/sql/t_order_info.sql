CREATE TABLE `t_order_info` (
                                `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
                                `title` varchar(256) DEFAULT NULL COMMENT '订单标题',
                                `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
                                `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
                                `product_id` bigint(20) DEFAULT NULL COMMENT '支付产品id',
                                `payment_type` varchar(20) DEFAULT NULL COMMENT '支付类型',
                                `total_fee` int(11) DEFAULT NULL COMMENT '订单金额(分)',
                                `code_url` varchar(50) DEFAULT NULL COMMENT '订单二维码连接',
                                `order_status` varchar(10) DEFAULT NULL COMMENT '订单状态',
                                `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;
