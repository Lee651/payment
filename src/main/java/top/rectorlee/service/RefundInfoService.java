package top.rectorlee.service;

import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 */
public interface RefundInfoService {
    RestResult insertByOrderNo(String orderNo, String reason);

    RestResult updateRefund(String body, String type, String orderStatus);
}
