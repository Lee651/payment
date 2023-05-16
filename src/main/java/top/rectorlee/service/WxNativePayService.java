package top.rectorlee.service;

import top.rectorlee.utils.RestResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lee
 */
public interface WxNativePayService {
    RestResult order(Long productId) throws Exception;

    RestResult callBack(HttpServletRequest request, HttpServletResponse response);

    RestResult cancelOrder(String orderNo) throws Exception;

    RestResult queryOrder(String orderNo) throws Exception;

    RestResult refund(String orderNo, String reason) throws Exception;

    RestResult refundCallBack(HttpServletRequest request, HttpServletResponse response);

    RestResult downloadBill(String billDate, String type) throws Exception;
}
