package top.rectorlee.service;

import top.rectorlee.entity.PaymentInfo;
import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 */
public interface PaymentInfoService {
    RestResult insertPaymentInfo(PaymentInfo paymentInfo);
}
