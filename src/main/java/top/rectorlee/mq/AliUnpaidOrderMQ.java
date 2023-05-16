package top.rectorlee.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.rectorlee.enums.OrderStatus;
import top.rectorlee.mapper.OrderInfoMapper;

import java.util.Map;

/**
 * @author Lee
 * @description
 * @date 2023-05-09  21:10:46
 */
@Slf4j
@Component
@Transactional
public class AliUnpaidOrderMQ {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @RabbitListener(queuesToDeclare = @Queue(durable = "true", autoDelete = "false", exclusive = "false", name = "ali-queue"))
    public void updateOrderStatus(Map<String, String> map) {
        String orderNo = map.get("orderNo");
        String orderStatus = map.get("orderStatus");

        if ("WAIT_BUYER_PAY".equals(orderStatus)) {
            log.info("交易创建, 等待买家付款");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.NOTPAY.getType());
        }

        if ("TRADE_CLOSE".equals(orderStatus)) {
            log.info("订单超时未支付");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.CLOSED.getType());
        }

        if ("TRADE_SUCCESS".equals(orderStatus)) {
            log.info("支付成功");
            orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.SUCCESS.getType());
        }
    }
}
