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
public class PaymentInfo {
    private Long id;

    // 商品订单编号
    private String orderNo;

    // 支付系统交易编号
    private String transactionId;

    // 支付类型
    private String paymentType;

    // 交易类型
    private String tradeType;

    // 交易状态
    private String tradeState;

    // 支付金额(分)
    private Integer payerTotal;

    // 通知参数
    private String content;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
