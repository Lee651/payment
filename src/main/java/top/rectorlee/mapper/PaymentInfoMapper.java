package top.rectorlee.mapper;

import org.springframework.stereotype.Repository;
import top.rectorlee.entity.PaymentInfo;

/**
 * @author Lee
 */
@Repository
public interface PaymentInfoMapper {
    void insertPaymentInfo(PaymentInfo paymentInfo);
}
