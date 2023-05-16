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
public class RefundInfo {
    private Long id;

    // 商品订单编号
    private String orderNo;

    // 退款单编号
    private String refundNo;

    // 支付系统退款单号
    private String refundId;

    // 原订单金额(分)
    private Integer totalFee;

    // 退款金额(分)
    private Integer refund;

    // 退款原因
    private String reason;

    // 退款单状态
    private String refundStatus;

    // 申请退款返回参数
    private String contentReturn;

    // 退款结果通知参数
    private String contentNotify;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
