package top.rectorlee.service;

import top.rectorlee.utils.RestResult;

import java.util.Map;

/**
 * @author Lee
 */
public interface AliWebPayService {
    RestResult tradePage(Long productId) throws Exception;

    RestResult callBack(Map<String, String> parameterMap) throws Exception;

    RestResult cancelOrder(String orderNo) throws Exception;

    RestResult queryOrder(String orderNo) throws Exception;

    RestResult refundOrder(String orderNo, String reason) throws Exception;

    RestResult downloadBill(String billDate, String type) throws Exception;
}
