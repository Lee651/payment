package top.rectorlee.service;

import top.rectorlee.entity.OrderInfo;
import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 */
public interface OrderInfoService {
    /**
     * 创建订单
     */
    RestResult createOrderByProductId(Long productId, String paymentType);

    /**
     * 修改订单
     */
    RestResult updateOrder(OrderInfo orderInfo);

    /**
     * 保存二维码
     */
    RestResult saveCodeUrlByOrderNo(String orderNo, String codeUrl);

    /**
     * 根据解密后的明文修改订单状态
     */
    void updateOrderStatusByPlainText(String plainText, String type);

    /**
     * 根据订单号查询订单状态
     */
    RestResult selectOrderStatusByOrderNo(String orderNo);

    /**
     * 查询订单列表
     */
    RestResult selectOrderList() throws Exception;

    /**
     * 根据订单号取消订单
     */
    RestResult cancelOrder(String orderNo);

    /**
     * 根据订单号和订单状态修改订单
     */
    RestResult updateByOrderNoAndOrderStatus(String orderNo, String orderStatus);

    /**
     * 根据订单号查询订单
     */
    RestResult selectOrderByOrderNo(String orderNo);
}
