package top.rectorlee.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import top.rectorlee.enums.OrderStatus;
import top.rectorlee.mapper.OrderInfoMapper;

import java.util.Map;

/**
 * @author Lee
 * @description
 * @date 2023-05-07  15:37:35
 */
@Slf4j
// @Component
@Transactional
public class WxUnpaidOrderMQ {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @RabbitListener(queuesToDeclare = @Queue(durable = "true", autoDelete = "false", exclusive = "false", name = "wx-queue"))
    public void updateOrderStatus(Map<String, String> map) {
        String orderNo = map.get("orderNo");
        String orderStatus = map.get("orderStatus");

        if ("NOTPAY".equals(orderStatus)) {
            // 如果交易状态为未支付, 则调用微信关单接口, 同时更新订单状态为超时未支付
            log.info("未支付 ===>");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.NOTPAY.getType());
            // TODO
        }

        if ("SUCCESS".equals(orderStatus)) {
            // 如果交易状态为支付成功, 则更新订单状态, 同时新增支付记录
            log.info("支付成功 ===>");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.SUCCESS.getType());
            // TODO
        }

        if ("CLOSED".equals(orderStatus)) {
            log.info("已关闭");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.CLOSED.getType());
        }

        if ("REFUND".equals(orderStatus)) {
            log.info("转入退款");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS.getType());
        }
    }
}
