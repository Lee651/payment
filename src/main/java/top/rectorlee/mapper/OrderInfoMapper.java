package top.rectorlee.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.rectorlee.entity.OrderInfo;

import java.util.List;

/**
 * @author Lee
 */
@Repository
public interface OrderInfoMapper {
    void updateOrder(OrderInfo orderInfo);

    OrderInfo selectByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId, @Param("orderStatus") String orderStatus);

    void insertOrderInfo(OrderInfo orderInfo);

    void saveCodeUrlByOrderNo(@Param("orderNo") String orderNo, @Param("codeUrl") String codeUrl);

    void updateOrderStatusByOrderNo(@Param("orderNo") String orderNo, @Param("orderStatus") String orderStatus);

    String selectOrderStatusByOrderNo(String orderNo);

    List<OrderInfo> selectOrderList();

    List<OrderInfo> selectUnpaidOrderList(String paymentType);

    OrderInfo selectByOrderNo(String orderNo);
}
