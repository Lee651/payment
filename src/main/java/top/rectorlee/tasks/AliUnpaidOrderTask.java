package top.rectorlee.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import top.rectorlee.entity.OrderInfo;
import top.rectorlee.enums.PayType;
import top.rectorlee.mapper.OrderInfoMapper;
import top.rectorlee.service.AliWebPayService;
import top.rectorlee.utils.RestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lee
 * @description 支付宝未支付订单
 * @date 2023-05-09  21:02:56
 */
@Slf4j
// @Component
public class AliUnpaidOrderTask {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private AliWebPayService aliWebPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 每隔60s监听一次
    @Scheduled(cron = "0/60 * * ? * *")
    public void aliUnpaidOrders() {
        log.info("开始执行支付宝定时任务");

        List<OrderInfo> list = orderInfoMapper.selectUnpaidOrderList(PayType.ALIPAY.getType());
        log.info("支付宝未支付订单: {}", list);

        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(l -> {
                String orderNo = l.getOrderNo();

                try {
                    // 调用微信查单接口核实订单状态
                    RestResult restResult = aliWebPayService.queryOrder(orderNo);
                    Map<String, Object> data = (Map<String, Object>) restResult.getData();
                    Map<String, String> map = new HashMap<>();
                    map.put("orderNo", orderNo);
                    map.put("orderStatus", (String) data.get("trade_state"));

                    log.info("给支付宝MQ发送的消息为: {}", map);
                    rabbitTemplate.convertAndSend("ali-queue", map);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
