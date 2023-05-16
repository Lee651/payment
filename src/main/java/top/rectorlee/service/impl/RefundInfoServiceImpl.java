package top.rectorlee.service.impl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.rectorlee.entity.OrderInfo;
import top.rectorlee.entity.RefundInfo;
import top.rectorlee.enums.PayType;
import top.rectorlee.mapper.OrderInfoMapper;
import top.rectorlee.mapper.RefundInfoMapper;
import top.rectorlee.service.RefundInfoService;
import top.rectorlee.utils.HttpStatus;
import top.rectorlee.utils.OrderNoUtils;
import top.rectorlee.utils.RestResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Lee
 */
@Slf4j
@Service
public class RefundInfoServiceImpl implements RefundInfoService {
    @Autowired
    private RefundInfoMapper refundInfoMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Transactional
    @Override
    public RestResult insertByOrderNo(String orderNo, String reason) {
        // 根据订单号查询订单信息
        OrderInfo orderInfo = orderInfoMapper.selectByOrderNo(orderNo);
        if (Objects.isNull(orderInfo)) {
            return new RestResult(HttpStatus.NOT_FOUND, "订单不存在");
        }

        log.info("订单详情: {}", orderInfo);

        RefundInfo refundInfo = RefundInfo.builder()
                .orderNo(orderNo)
                .refundNo(OrderNoUtils.getRefundNo())
                .totalFee(orderInfo.getTotalFee())
                .refund(orderInfo.getTotalFee())
                .reason(reason)
                .createTime(new Date())
                .build();

        refundInfoMapper.insertRefundInfo(refundInfo);
        log.info("退款信息为： {}", refundInfo);

        return new RestResult<>(HttpStatus.SUCCESS, "退款申请成功", refundInfo);
    }

    @Transactional
    @Override
    public RestResult updateRefund(String body, String type, String orderStatus) {
        Gson gson = new Gson();
        Map<String, Object> dataMap = gson.fromJson(body, HashMap.class);
        if (PayType.WXPAY.getType().equals(type)) {
            // 支付系统退款单号
            String refundId = (String) dataMap.get("refund_id");
            // 商户退款单号
            String refundNo = (String) dataMap.get("out_refund_no");

            // 根据退款单号查询退款单
            RefundInfo refundInfo = refundInfoMapper.selectByRefundNo(refundNo);

            if (Objects.nonNull(refundInfo)) {
                refundInfo.setRefundId(refundId);
                // 从申请退款接口中获取退款状态
                if (dataMap.get("status") != null) {
                    /*orderStatus = (String) dataMap.get("status");*/
                    refundInfo.setContentReturn(body);
                }
                // 从退款结果通知中获取退款状态
                if (dataMap.get("refund_status") != null) {
                    /*orderStatus = (String) dataMap.get("refund_status");*/
                    refundInfo.setContentNotify(body);
                }
                refundInfo.setRefundStatus(orderStatus);

                refundInfoMapper.updateRefundInfo(refundInfo);

                log.info("退款单更新成功: {}", refundInfo);

                return new RestResult(HttpStatus.SUCCESS, "退款单更新成功");
            }
        } else {
            Map<String, String> map = (Map<String, String>) dataMap.get("alipay_trade_refund_response");
            // 支付宝交易号
            String tradeNo = map.get("trade_no");
            // 退单号
            String refundNo = (String) dataMap.get("refund_no");

            // 根据订单号查询退款单
            RefundInfo refundInfo = refundInfoMapper.selectByRefundNo(refundNo);

            if (Objects.nonNull(refundInfo)) {
                refundInfo.setRefundId(tradeNo);
                refundInfo.setRefundStatus(orderStatus);
                refundInfo.setContentNotify(body);
                refundInfo.setContentReturn(body);

                refundInfoMapper.updateRefundInfo(refundInfo);

                log.info("退款单更新成功: {}", refundInfo);

                return new RestResult(HttpStatus.SUCCESS, "退款单更新成功");
            }
        }

        return new RestResult(HttpStatus.ERROR, "退款单不存在");
    }
}
