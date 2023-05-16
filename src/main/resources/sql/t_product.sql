CREATE TABLE `t_product` (
                             `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品id',
                             `title` varchar(20) DEFAULT NULL COMMENT '商品名称',
                             `price` int(11) DEFAULT NULL COMMENT '价格（分）',
                             `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

INSERT INTO t_product(`id`, `title`, `price`, `create_time`, `update_time`) VALUES (1, 'Java课程', 1, '2023-05-04 18:12:02', '2023-05-04 18:12:02');
INSERT INTO t_product(`id`, `title`, `price`, `create_time`, `update_time`) VALUES (2, '大数据课程', 1, '2023-05-04 18:12:02', '2023-05-04 18:12:02');
INSERT INTO t_product(`id`, `title`, `price`, `create_time`, `update_time`) VALUES (3, '前端课程', 1, '2023-05-04 18:12:02', '2023-05-04 18:12:02');
INSERT INTO t_product(`id`, `title`, `price`, `create_time`, `update_time`) VALUES (4, 'UI课程', 1, '2023-05-04 18:12:02', '2023-05-04 18:12:02');
