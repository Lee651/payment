package top.rectorlee.service.impl;

import com.google.gson.Gson;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.rectorlee.config.WxPayConfig;
import top.rectorlee.entity.OrderInfo;
import top.rectorlee.entity.RefundInfo;
import top.rectorlee.enums.OrderStatus;
import top.rectorlee.enums.PayType;
import top.rectorlee.enums.wxpay.WxApiType;
import top.rectorlee.enums.wxpay.WxNotifyType;
import top.rectorlee.service.*;
import top.rectorlee.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Lee
 * @description 微信Native接口类
 * @date 2023-05-05  16:25:42
 */
@Slf4j
@Service
public class WxNativePayServiceImpl implements WxNativePayService {
    @Autowired
    private WxPayConfig wxPayConfig;

    @Autowired
    private CloseableHttpClient wxPayClient;

    @Autowired
    private CloseableHttpClient wxPayNoSignClient;

    @Autowired
    private Verifier verifier;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private RefundInfoService refundInfoService;

    @Transactional
    @Override
    public RestResult order(Long productId) throws Exception {
        // 生成订单
        RestResult restResult = orderInfoService.createOrderByProductId(productId, PayType.WXPAY.getType());
        OrderInfo orderInfo = (OrderInfo) restResult.getData();
        log.info("订单详情为: {}", orderInfo);

        // 如果订单中存在二维码, 则直接返回
        String codeUrl = orderInfo.getCodeUrl();
        if (!StringUtils.isEmpty(codeUrl)) {
            log.info("二维码已存在");

            Map<String, String> map = new HashMap<>();
            map.put("codeUrl", codeUrl);
            map.put("orderNo", orderInfo.getOrderNo());

            return new RestResult<>(restResult.getCode(), restResult.getMsg(), map);
        }

        // 调用微信接口返回二维码地址
        HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.NATIVE_PAY.getType()));
        // 设置请求参数
        Map<String, Object> requestDataMap = new HashMap<>();
        requestDataMap.put("appid", wxPayConfig.getAppid());
        requestDataMap.put("mchid", wxPayConfig.getMchId());
        requestDataMap.put("description", orderInfo.getTitle());
        requestDataMap.put("out_trade_no", orderInfo.getOrderNo());
        requestDataMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.NATIVE_NOTIFY.getType()));
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("total", orderInfo.getTotalFee());
        amountMap.put("currency", "CNY");
        requestDataMap.put("amount", amountMap);
        Gson gson = new Gson();
        String reqdata = gson.toJson(requestDataMap);
        log.info("调用微信Native下单接口, 参数: {}", reqdata);
        StringEntity entity = new StringEntity(reqdata,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) { // 处理成功
                log.info("微信Native下单接口调用成功, 响应体为: {} ", body);

                HashMap resultMap = gson.fromJson(body, HashMap.class);
                Map<String, Object> result = new HashMap<>();
                codeUrl = (String) resultMap.get("code_url");
                result.put("codeUrl", codeUrl);
                result.put("orderNo", orderInfo.getOrderNo());

                // 存储二维码
                orderInfoService.saveCodeUrlByOrderNo(orderInfo.getOrderNo(), codeUrl);

                return new RestResult<>(HttpStatus.SUCCESS, "微信Native下单接口调用成功", result);
            } else if (statusCode == 204) { // 处理成功但无返回Body
                log.info("微信Native下单接口调用成功");

                return new RestResult(HttpStatus.SUCCESS, "微信Native下单接口调用成功");
            } else {
                log.info("微信Native下单接口调用失败, 响应码为: {}, 响应体为: {} ", statusCode, body);

                throw new Exception("微信Native下单接口调用失败");
            }
        } finally {
            response.close();
        }
    }

    @Override
    public RestResult callBack(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();

        try {
            // 获取请求参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String id = (String) bodyMap.get("id");
            log.info("微信支付回调通知: {}", body);

            // 签名验证
            WxRequestVerification wxRequestVerification = new WxRequestVerification(verifier, id, body);
            // 验签失败
            if (!wxRequestVerification.validate(request)) {
                log.info("支付验签失败");

                Map<String, Object> map = new HashMap<>();
                map.put("code", "FAIL");
                map.put("message", "失败");
                String json = gson.toJson(map);

                return new RestResult<>(HttpStatus.ERROR, "支付失败", json);
            }

            // 密文解密
            String msg = "支付";
            String plainText = decrypt(bodyMap, msg);
            // 根据解密后的明文修改订单状态
            orderInfoService.updateOrderStatusByPlainText(plainText, msg);

            // 响应微信应答
            Map<String, Object> map = new HashMap<>();
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            String json = gson.toJson(map);

            return new RestResult<>(HttpStatus.SUCCESS, "支付成功", json);
        } catch (Exception e) {
            e.printStackTrace();

            log.info("支付回调出现异常");

            // 响应微信应答
            Map<String, Object> map = new HashMap<>();
            map.put("code", "FAIL");
            map.put("message", "失败");
            String json = gson.toJson(map);

            return new RestResult<>(HttpStatus.ERROR, "支付回调失败", json);
        }
    }

    @Transactional
    @Override
    public RestResult cancelOrder(String orderNo) throws Exception {
        // 调用微信支付的关单接口
        wxCloseOrder(orderNo);

        // 更新订单状态
        return orderInfoService.cancelOrder(orderNo);
    }

    @Override
    public RestResult queryOrder(String orderNo) throws Exception {
        String url = wxPayConfig.getDomain().concat(String.format(WxApiType.ORDER_QUERY_BY_NO.getType(), orderNo)).concat("?mchid=").concat(wxPayConfig.getMchId());
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());

            if (statusCode == 200) {
                log.info("微信Native查单接口调用成功, 响应体为: {} ", body);

                return new RestResult<>(HttpStatus.SUCCESS, "微信Native查单接口调用成功", body);
            } else if (statusCode == 204) {
                log.info("微信Native查单接口调用成功");

                return new RestResult(HttpStatus.SUCCESS, "微信Native查单接口调用成功");
            } else {
                log.info("微信Native查单接口调用失败, 响应码为: {}, 响应体为: {} ", statusCode, body);

                throw new Exception("微信Native查单接口调用失败");
            }
        } finally {
            response.close();
        }
    }

    @Transactional
    @Override
    public RestResult refund(String orderNo, String reason) throws Exception {
        // 生成退款单
        RestResult restResult = refundInfoService.insertByOrderNo(orderNo, reason);
        RefundInfo refundInfo = (RefundInfo) restResult.getData();

        if (Objects.nonNull(refundInfo)) {
            // 调用微信Native申请退款接口
            HttpPost httpPost = new HttpPost(wxPayConfig.getDomain().concat(WxApiType.DOMESTIC_REFUNDS.getType()));
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("out_trade_no", orderNo);
            paramsMap.put("out_refund_no", refundInfo.getRefundNo());
            Map<String, Object> amountMap = new HashMap<>();
            amountMap.put("refund", refundInfo.getTotalFee());
            amountMap.put("total", refundInfo.getTotalFee());
            amountMap.put("currency", "CNY");
            paramsMap.put("amount", amountMap);
            paramsMap.put("notify_url", wxPayConfig.getNotifyDomain().concat(WxNotifyType.REFUND_NOTIFY.getType()));

            Gson gson = new Gson();
            String reqdata = gson.toJson(paramsMap);
            log.info("调用微信Native退款接口, 参数: {}", reqdata);

            StringEntity entity = new StringEntity(reqdata,"utf-8");
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");

            // 完成签名并执行请求
            CloseableHttpResponse response = wxPayClient.execute(httpPost);

            try {
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) { // 处理成功
                    log.info("微信Native退款接口调用成功, 响应体为: {} ", body);

                    String orderStatus = OrderStatus.REFUND_PROCESSING.getType();
                    // 更新订单状态
                    orderInfoService.updateByOrderNoAndOrderStatus(orderNo, orderStatus);

                    // 更新退款单
                    refundInfoService.updateRefund(body, PayType.WXPAY.getType(), orderStatus);

                    return new RestResult<>(HttpStatus.SUCCESS, "微信Native退款接口调用成功");
                } else if (statusCode == 204) { // 处理成功但无返回Body
                    log.info("微信Native退款接口调用成功");

                    return new RestResult(HttpStatus.SUCCESS, "微信Native退款接口调用成功");
                } else {
                    log.info("微信Native退款接口调用失败, 响应码为: {}, 响应体为: {} ", statusCode, body);

                    throw new Exception("微信Native退款接口调用失败");
                }
            } finally {
                response.close();
            }
        }

        return new RestResult(HttpStatus.ERROR, "退款失败");
    }

    @Transactional
    @Override
    public RestResult refundCallBack(HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new Gson();

        try {
            // 获取请求参数
            String body = HttpUtils.readData(request);
            Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
            String id = (String) bodyMap.get("id");
            log.info("微信退款回调通知: {}", body);

            // 签名验证
            WxRequestVerification wxRequestVerification = new WxRequestVerification(verifier, id, body);
            // 验签失败
            if (!wxRequestVerification.validate(request)) {
                log.info("退款验签失败");

                Map<String, Object> map = new HashMap<>();
                map.put("code", "FAIL");
                map.put("message", "失败");
                String json = gson.toJson(map);

                return new RestResult<>(HttpStatus.ERROR, "退款失败", json);
            }

            // 密文解密
            String msg = "退款";
            String plainText = decrypt(bodyMap, msg);
            // 根据解密后的明文修改订单状态
            orderInfoService.updateOrderStatusByPlainText(plainText, msg);

            // 更新退款单状态
            refundInfoService.updateRefund(plainText, PayType.WXPAY.getType(), OrderStatus.REFUND_SUCCESS.getType());

            // 响应微信应答
            Map<String, Object> map = new HashMap<>();
            map.put("code", "SUCCESS");
            map.put("message", "成功");
            String json = gson.toJson(map);

            return new RestResult<>(HttpStatus.SUCCESS, "退款成功", json);
        } catch (Exception e) {
            e.printStackTrace();

            log.info("退款回调出现异常");

            // 响应微信应答
            Map<String, Object> map = new HashMap<>();
            map.put("code", "FAIL");
            map.put("message", "失败");
            String json = gson.toJson(map);

            return new RestResult<>(HttpStatus.ERROR, "退款回调失败", json);
        }
    }

    @Override
    public RestResult downloadBill(String billDate, String type) throws Exception {
        String url = "";
        if ("tradebill".equals(type)) {// 下载交易账单
            url = wxPayConfig.getDomain().concat(WxApiType.TRADE_BILLS.getType()).concat("?bill_date=").concat(billDate);
        } else if ("fundflowbill".equals(type)) {// 下载资金账单
            url = wxPayConfig.getDomain().concat(WxApiType.FUND_FLOW_BILLS.getType()).concat("?bill_date=").concat(billDate);
        } else {
            throw new Exception("不支持的账单类型");
        }

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");
        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpGet);

        String msg = ("tradebill".equals(type) ? "交易账单" : "资金帐单");

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());
            if (statusCode == 200) {
                log.info("微信Native申请{}调用成功, 响应体为: {}", msg, body);

                // 获取下载交易账单/资金账单地址
                Gson gson = new Gson();
                Map<String, String> bodyMap = gson.fromJson(body, HashMap.class);
                String downloadUrl = bodyMap.get("download_url");
                // 根据微信返回的下载地址下载数据
                String downloadData = download(downloadUrl, msg);
                if (StringUtils.isEmpty(downloadData)) {
                    return new RestResult(HttpStatus.ERROR, "暂无数据");
                }

                return new RestResult<>(HttpStatus.SUCCESS, "下载" + msg + "成功", downloadData);
            }

            return new RestResult(HttpStatus.ERROR, "下载" + msg + "失败");
        } finally {
            response.close();
        }
    }

    /**
     * 调用微信支付关单接口
     */
    private void wxCloseOrder(String orderNo) throws Exception {
        String url = wxPayConfig.getDomain().concat(String.format(WxApiType.CLOSE_ORDER_BY_NO.getType(), orderNo));
        HttpPost httpPost = new HttpPost(url);

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        map.put("mchid", wxPayConfig.getMchId());
        String gsonJson = gson.toJson(map);
        log.info("微信Native关单接口的参数为: {}", gsonJson);

        StringEntity entity = new StringEntity(gsonJson,"utf-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayClient.execute(httpPost);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 204) {
                log.info("微信Native关单接口调用成功");
            } else {
                log.info("微信Native关单接口调用失败");

                throw new Exception("微信Native关单接口调用失败");
            }
        } finally {
            response.close();
        }
    }

    /**
     * 密文解密
     */
    private String decrypt(Map<String, Object> bodyMap, String type) throws Exception {
        log.info("密文解密");

        // 通知数据
        Map<String, String> resourceMap = (Map<String, String>) bodyMap.get("resource");
        // 通知数据中的附加数据
        String associatedData = resourceMap.get("associated_data");
        // 通知数据中的随机串
        String nonce = resourceMap.get("nonce");
        // 通知数据中的数据密文
        String cipherText = resourceMap.get("ciphertext");
        log.info("微信{}中的通知数据密文为: {}", type, cipherText);

        AesUtil aesUtil = new AesUtil(wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        // 解密后的明文
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8), cipherText);
        log.info("解密后的明文为: {}", plainText);

        return plainText;
    }

    /**
     * 获取下载数据
     */
    private String download(String downloadUrl, String msg) throws Exception {
        log.info("下载{}的地址为: {}", msg, downloadUrl);

        HttpGet httpGet = new HttpGet(downloadUrl);
        httpGet.addHeader("Accept", "application/json");

        // 完成签名并执行请求
        CloseableHttpResponse response = wxPayNoSignClient.execute(httpGet);

        try {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity());
            if (statusCode == 200) {
                log.info("下载{}的数据为: {}", msg, body);
                return body;
            }

            log.info("下载{}失败", msg);
            return null;
        } finally {
            response.close();
        }
    }
}
