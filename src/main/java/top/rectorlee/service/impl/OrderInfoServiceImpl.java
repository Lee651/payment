package top.rectorlee.service.impl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.rectorlee.entity.*;
import top.rectorlee.enums.OrderStatus;
import top.rectorlee.enums.PayType;
import top.rectorlee.mapper.*;
import top.rectorlee.service.OrderInfoService;
import top.rectorlee.utils.*;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Lee
 */
@Slf4j
@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    @Qualifier("asyncExecutor")
    private TaskExecutor taskExecutor;

    private final ReentrantLock lock = new ReentrantLock();

    @Transactional
    @Override
    public RestResult createOrderByProductId(Long productId, String paymentType) {
        log.info("商品id为: {}", productId);

        // 当前系统登录对象id
        Long userId = 1L;
        // 每次创建订单时先查询该用户下是否有未支付的订单, 防止重复下单
        OrderInfo order = orderInfoMapper.selectByProductIdAndUserId(productId, userId, OrderStatus.NOTPAY.getType());

        if (!Objects.isNull(order)) {
            log.info("订单{}已存在，请勿重复提交。", order);

            return new RestResult<>(HttpStatus.SUCCESS, "订单已存在, 请勿重复提交", order);
        }

        // 根据商品id查询商品信息
        Product product = productMapper.selectByProductId(productId);

        // 生成订单
        OrderInfo orderInfo = OrderInfo.builder()
                .orderNo(OrderNoUtils.getOrderNo())
                .orderStatus(OrderStatus.NOTPAY.getType())
                .title(product.getTitle())
                .productId(productId)
                .paymentType(paymentType)
                .userId(1L)
                .totalFee(product.getPrice())
                .createTime(new Date())
                .build();

        log.info("订单{}创建成功。", orderInfo);
        orderInfoMapper.insertOrderInfo(orderInfo);

        return new RestResult<>(HttpStatus.SUCCESS, "订单创建成功", orderInfo);
    }

    @Transactional
    @Override
    public RestResult updateOrder(OrderInfo orderInfo) {
        log.info("更新订单: {}", orderInfo);
        orderInfoMapper.updateOrder(orderInfo);

        return new RestResult(HttpStatus.SUCCESS, "订单更新成功");
    }

    @Transactional
    @Override
    public RestResult saveCodeUrlByOrderNo(String orderNo, String codeUrl) {
        log.info("订单号为: {}, 二维码地址为: {}", orderNo, codeUrl);
        orderInfoMapper.saveCodeUrlByOrderNo(orderNo, codeUrl);

        return new RestResult(HttpStatus.SUCCESS, "二维码保存成功");
    }

    @Transactional
    @Override
    public void updateOrderStatusByPlainText(String plainText, String type) {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(plainText, HashMap.class);

        // 订单号
        String orderNo = (String) map.get("out_trade_no");

        // 防止多个线程并发修改订单状态, 同时插入多条支付记录
        if (lock.tryLock()) {
            try {
                // 先根据订单号检查订单是否修改成功, 避免重复更新数据, 同时避免支付记录表一直添加数据(接口幂等性处理)
                String orderStatus = orderInfoMapper.selectOrderStatusByOrderNo(orderNo);
                if ("支付".equals(type)) {
                    if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
                        log.info("订单状态已更新");

                        return;
                    }
                    // 更新订单状态
                    orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.SUCCESS.getType());
                    log.info("订单状态更新成功, 订单号为: {}, 订单状态改为: {}", orderNo, OrderStatus.SUCCESS.getType());

                    // 支付系统交易编号
                    String transactionId = (String) map.get("transaction_id");
                    // 支付类型
                    String paymentType = PayType.WXPAY.getType();
                    // 交易类型
                    String tradeType = (String) map.get("trade_type");
                    // 交易状态
                    String tradeState = (String) map.get("trade_state");
                    // 支付金额
                    Map<String, Object> amountMap = (Map<String, Object>) map.get("amount");
                    Integer payerTotal = ((Double) amountMap.get("payer_total")).intValue();

                    // 添加支付记录
                    PaymentInfo paymentInfo = PaymentInfo.builder().orderNo(orderNo).transactionId(transactionId).paymentType(paymentType).tradeType(tradeType).tradeState(tradeState).payerTotal(payerTotal).content(plainText).createTime(new Date()).build();
                    log.info("新增支付记录: {}", paymentInfo);
                    paymentInfoMapper.insertPaymentInfo(paymentInfo);
                }

                if ("退款".equals(type)) {
                    if (!OrderStatus.REFUND_PROCESSING.getType().equals(orderStatus)) {
                        return;
                    }

                    // 更新订单状态
                    orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.REFUND_SUCCESS.getType());
                    log.info("订单状态更新成功, 订单号为: {}, 订单状态改为: {}", orderNo, OrderStatus.REFUND_SUCCESS.getType());
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public RestResult selectOrderStatusByOrderNo(String orderNo) {
        log.info("根据订单号查询订单状态, 订单号为: {}", orderNo);

        String orderStatus = orderInfoMapper.selectOrderStatusByOrderNo(orderNo);
        if (OrderStatus.SUCCESS.getType().equals(orderStatus)) {
            return new RestResult(HttpStatus.SUCCESS, "支付成功");
        }

        return new RestResult(HttpStatus.PAY_PROGRESS, "支付中");
    }

    @Override
    public RestResult selectOrderList() throws Exception{
        List<OrderInfo> list = orderInfoMapper.selectOrderList();
        log.info("订单列表为: {}", list);

        // 异步输出所有订单信息
        // asyncTask(list);
        // log.info(asyncTask(list).get());
        asyncTask(list);

        return new RestResult<>(HttpStatus.SUCCESS, "订单列表查询成功", list, list.size());
    }

    /**
     * 异步调用方式一
     */
    /*@Async
    public void asyncTask(List<OrderInfo> list) {
        if (!CollectionUtils.isEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                log.info("订单{}信息为: {}", i + 1, list.get(i));
            }
        }
    }*/

    /**
     * 异步调用方式二
     */
    /*public CompletableFuture<String> asyncTask(List<OrderInfo> list) {
        return CompletableFuture.supplyAsync(() -> {
            if (!CollectionUtils.isEmpty(list)) {
                for (int i = 0; i < list.size(); i++) {
                    log.info("订单{}信息为: {}", i + 1, list.get(i));
                }

                return "异步任务执行完成";
            }

            return "集合为空";
        });
    }*/

    /**
     * 异步调用方式三
     */
    public void asyncTask(List<OrderInfo> list) {
        taskExecutor.execute(() -> {
            if (!CollectionUtils.isEmpty(list)) {
                for (int i = 0; i < list.size(); i++) {
                    log.info("订单{}信息为: {}", i + 1, list.get(i));
                }
            }
        });
    }

    @Transactional
    @Override
    public RestResult cancelOrder(String orderNo) {
        log.info("根据订单号取消订单, 订单号为: {}", orderNo);
        orderInfoMapper.updateOrderStatusByOrderNo(orderNo, OrderStatus.CANCEL.getType());
        log.info("订单已取消");

        return new RestResult(HttpStatus.SUCCESS, "订单已取消");
    }

    @Transactional
    @Override
    public RestResult updateByOrderNoAndOrderStatus(String orderNo, String orderStatus) {
        log.info("订单号为: {}, 订单状态为: {}", orderNo, orderStatus);
        orderInfoMapper.updateOrderStatusByOrderNo(orderNo, orderStatus);

        return new RestResult(HttpStatus.SUCCESS, "订单状态修改成功");
    }

    @Override
    public RestResult selectOrderByOrderNo(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.selectByOrderNo(orderNo);

        return new RestResult<>(HttpStatus.SUCCESS, "订单查询成功", orderInfo);
    }
}
