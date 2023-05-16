package top.rectorlee.tasks;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import top.rectorlee.entity.OrderInfo;
import top.rectorlee.enums.PayType;
import top.rectorlee.mapper.OrderInfoMapper;
import top.rectorlee.service.WxNativePayService;
import top.rectorlee.utils.RestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lee
 * @description 微信未支付订单
 * @date 2023-05-07  15:19:12
 */
// @Component
@Slf4j
public class WxUnpaidOrderTask {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private WxNativePayService wxNativePayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 每隔60s监听一次
    @Scheduled(cron = "0/60 * * ? * *")
    public void wxUnpaidOrders() {
        log.info("开始执行微信定时任务");

        List<OrderInfo> list = orderInfoMapper.selectUnpaidOrderList(PayType.WXPAY.getType());
        log.info("微信未支付订单: {}", list);

        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(l -> {
                String orderNo = l.getOrderNo();

                try {
                    // 调用微信查单接口核实订单状态
                    RestResult restResult = wxNativePayService.queryOrder(orderNo);
                    String data = (String) restResult.getData();
                    Gson gson = new Gson();
                    Map<String, Object> resultMap = gson.fromJson(data, HashMap.class);
                    Map<String, String> map = new HashMap<>();
                    map.put("orderNo", orderNo);
                    map.put("orderStatus", (String) resultMap.get("trade_state"));

                    log.info("给微信MQ发送的消息为: {}", map);
                    rabbitTemplate.convertAndSend("wx-queue", map);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
