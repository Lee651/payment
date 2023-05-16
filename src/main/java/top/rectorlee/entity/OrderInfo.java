package top.rectorlee.entity;

import lombok.*;

import java.util.Date;

/**
 * @author Lee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderInfo {
    private Long id;

    // 订单标题
    private String title;

    // 商户订单编号
    private String orderNo;

    // 用户id
    private Long userId;

    // 支付产品id
    private Long productId;

    // 支付类型
    private String paymentType;

    // 订单金额(分)
    private Integer totalFee;

    // 订单二维码连接
    private String codeUrl;

    // 订单状态
    private String orderStatus;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
