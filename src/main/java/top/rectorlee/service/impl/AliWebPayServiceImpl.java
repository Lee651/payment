package top.rectorlee.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.rectorlee.entity.OrderInfo;
import top.rectorlee.entity.PaymentInfo;
import top.rectorlee.entity.RefundInfo;
import top.rectorlee.enums.OrderStatus;
import top.rectorlee.enums.PayType;
import top.rectorlee.enums.alipay.AliPayTradeState;
import top.rectorlee.service.AliWebPayService;
import top.rectorlee.service.OrderInfoService;
import top.rectorlee.service.PaymentInfoService;
import top.rectorlee.service.RefundInfoService;
import top.rectorlee.utils.HttpStatus;
import top.rectorlee.utils.RestResult;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Lee
 * @description
 * @date 2023-05-08  18:09:24
 */
@Slf4j
@Service
public class AliWebPayServiceImpl implements AliWebPayService {
    @Autowired
    private Environment environment;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private RefundInfoService refundInfoService;

    private final ReentrantLock lock = new ReentrantLock();

    @Transactional
    @Override
    public RestResult tradePage(Long productId) throws Exception {
        // 生成订单
        RestResult restResult = orderInfoService.createOrderByProductId(productId, PayType.ALIPAY.getType());
        OrderInfo orderInfo = (OrderInfo) restResult.getData();

        // 调用支付宝下单接口
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 支付成功后跳转地址
        request.setReturnUrl(environment.getProperty("alipay.return-url"));
        // 支付宝平台回调地址
        request.setNotifyUrl(environment.getProperty("alipay.notify-url"));

        // 公共参数设置方式一
        /*JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderInfo.getOrderNo());
        //支付金额，最小值0.01元
        BigDecimal total = new BigDecimal(orderInfo.getTotalFee().toString()).divide(new BigDecimal("100"));
        bizContent.put("total_amount", total);
        // 订单标题
        bizContent.put("subject", orderInfo.getTitle());
        // 电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());*/

        // 公共参数设置方式二
        AlipayTradePagePayModel payModel = new AlipayTradePagePayModel();
        payModel.setOutTradeNo(orderInfo.getOrderNo());
        payModel.setTotalAmount(new BigDecimal(orderInfo.getTotalFee().toString()).divide(new BigDecimal("100")).toString());
        payModel.setSubject(orderInfo.getTitle());
        payModel.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(payModel);

        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            log.info("支付宝下单接口调用成功, 返回结果为: {}", response.getBody());

            return new RestResult<>(HttpStatus.SUCCESS, "支付宝下单接口调用成功", response.getBody());
        } else {
            log.info("支付宝下单接口调用失败");

            return new RestResult<>(HttpStatus.ERROR, "支付宝下单接口调用失败");
        }
    }

    @Transactional
    @Override
    public RestResult callBack(Map<String, String> parameterMap) throws Exception {
        log.info("开始支付回调");

        /**
         * 调用SDK验证签名
         * 参数一: 请求参数
         * 参数二: 支付宝公钥
         * 参数三: 编码方式
         * 参数四: 签名类型
         */
        boolean signVerified = AlipaySignature.rsaCheckV1(parameterMap,
                environment.getProperty("alipay.alipay-public-key"),
                AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);
        if (!signVerified) {
            log.warn("签名校验失败");

            return new RestResult(HttpStatus.ERROR, "failure");
        }

        log.info("签名校验成功");
        log.info("回调请求参数为: {}", parameterMap);

        // 二次校验
        // 根据回调通知中的out_trade_no查询是否存在该订单
        String outTradeNo = parameterMap.get("out_trade_no");
        RestResult restResult = orderInfoService.selectOrderByOrderNo(outTradeNo);
        OrderInfo orderInfo = (OrderInfo) restResult.getData();
        if (Objects.isNull(orderInfo)) {
            log.warn("订单不存在");
            return new RestResult(HttpStatus.ERROR, "failure");
        }
        // 对比回调通知中的total_amount与查询出的订单的total_fee对比
        Integer totalAmountInt = new BigDecimal(parameterMap.get("total_amount")).multiply(new BigDecimal("100")).intValue();
        Integer totalFee = orderInfo.getTotalFee();
        if (!totalAmountInt.equals(totalFee)) {
            log.warn("金额校验失败");
            return new RestResult(HttpStatus.ERROR, "failure");
        }
        // 对比回调通知中的seller_id是否为商户自身
        String sellerId = parameterMap.get("seller_id");
        String property = environment.getProperty("alipay.seller-id");
        if (property == null || !property.equals(sellerId)) {
            log.warn("商家pid校验失败");
            return new RestResult(HttpStatus.ERROR, "failure");
        }
        // 对比回调通知中的app_id是否为商户自身
        String environmentProperty = environment.getProperty("alipay.app-id");
        String appId = parameterMap.get("app_id");
        if (environmentProperty == null || !environmentProperty.equals(appId)) {
            log.warn("appId校验失败");

            return new RestResult(HttpStatus.ERROR, "failure");
        }

        String tradeStatus = parameterMap.get("trade_status");
        if (!AliPayTradeState.SUCCESS.getType().equals(tradeStatus)) {
            log.warn("支付失败");

            return new RestResult(HttpStatus.ERROR, "failure");
        }

        log.info("支付成功");

        // 防止支付宝重复回调导致订单状态一直修改, 支付记录一直增加的情况
        if (lock.tryLock()) {
            try {
                // 根据订单号查询订单状态
                int code = orderInfoService.selectOrderStatusByOrderNo(outTradeNo).getCode();
                if (HttpStatus.SUCCESS == code) {
                    log.info("订单状态已更新");
                    return new RestResult<>(HttpStatus.SUCCESS, "success");
                }

                // 支付成功, 修改订单状态
                orderInfoService.updateByOrderNoAndOrderStatus(outTradeNo, OrderStatus.SUCCESS.getType());

                // 新增支付记录
                PaymentInfo paymentInfo = PaymentInfo
                        .builder()
                        .orderNo(outTradeNo)
                        .transactionId(parameterMap.get("trade_no"))
                        .paymentType(PayType.ALIPAY.getType())
                        .tradeType("电脑网站支付")
                        .tradeState(parameterMap.get("trade_status"))
                        .payerTotal(totalAmountInt)
                        .content(new Gson().toJson(parameterMap))
                        .createTime(new Date()).build();
                paymentInfoService.insertPaymentInfo(paymentInfo);

                return new RestResult<>(HttpStatus.SUCCESS, "success");
            } finally {
                lock.unlock();
            }
        }

        return new RestResult(HttpStatus.ERROR, "failure");
    }

    @Transactional
    @Override
    public RestResult cancelOrder(String orderNo) throws Exception {
        // 调用支付宝关单接口
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("trade_no", orderNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            log.info("支付宝关单接口调用成功");
        }

        // 更新订单状态
        orderInfoService.updateByOrderNoAndOrderStatus(orderNo, OrderStatus.CANCEL.getType());
        log.info("订单已取消, 订单号为: {}", orderNo);

        return new RestResult(HttpStatus.SUCCESS, "订单取消成功");
    }

    @Override
    public RestResult queryOrder(String orderNo) throws Exception {
        // 调用支付宝查单接口
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);

        log.info("调用支付宝查单接口");
        Gson gson = new Gson();
        String responseBody = response.getBody();
        Map<String, Object> respMap = gson.fromJson(responseBody, HashMap.class);
        Map<String, String> aliQueryResponse = (Map<String, String>) respMap.get("alipay_trade_query_response");
        String msg = "";
        if (response.isSuccess()) {
            log.info("支付宝查单接口调用成功");

            String tradeStatus = (String) aliQueryResponse.get("trade_status");

            if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                msg = "交易创建, 等待买家付款";
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                msg = "未付款";
            } else if ("TRADE_SUCCESS".equals(tradeStatus)) {
                msg = "交易支付成功";
            } else if ("TRADE_FINISHED".equals(tradeStatus)) {
                msg = "交易结束, 不可退款";
            }
        } else {
            msg = aliQueryResponse.get("sub_msg");
            log.info("支付宝查单接口调用成功, 但是{}", msg);
        }

        return new RestResult<>(HttpStatus.SUCCESS, msg, aliQueryResponse);
    }

    /*@Transactional*/
    @Override
    public RestResult refundOrder(String orderNo, String reason) throws Exception {
        // 创建退款单
        RestResult restResult = refundInfoService.insertByOrderNo(orderNo, reason);
        RefundInfo refundInfo = (RefundInfo) restResult.getData();

        // 调用支付宝退款接口
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderNo);
        bizContent.put("refund_amount", new BigDecimal(refundInfo.getTotalFee()).divide(new BigDecimal(100)).doubleValue());
        request.setBizContent(bizContent.toString());

        AlipayTradeRefundResponse response = alipayClient.execute(request);
        String orderStatus = "";
        if (response.isSuccess()) {
            log.info("支付宝退款接口调用成功");
            orderStatus = OrderStatus.REFUND_SUCCESS.getType();
        } else {
            log.info("支付宝退款接口调用失败");
            orderStatus = OrderStatus.REFUND_ABNORMAL.getType();
        }

        // 更新订单状态
        orderInfoService.updateByOrderNoAndOrderStatus(orderNo, orderStatus);

        // 更新退款单状态
        String body = response.getBody();
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(body, HashMap.class);
        map.put("refund_no", refundInfo.getRefundNo());
        String gsonJson = gson.toJson(map);
        refundInfoService.updateRefund(gsonJson, PayType.ALIPAY.getType(), orderStatus);

        return new RestResult(HttpStatus.SUCCESS, orderStatus);
    }

    @Override
    public RestResult downloadBill(String billDate, String type) throws Exception{
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("bill_date", billDate);
        bizContent.put("bill_type", type);
        request.setBizContent(bizContent.toString());
        AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.execute(request);
        String msgType = ("trade".equals(type) ? "交易账单" : "资金帐单");

        if (response.isSuccess()) {
            log.info("支付宝{}接口调用成功", msgType);
            String body = response.getBody();
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(body, HashMap.class);
            Map<String, String> data = (Map<String, String>) map.get("alipay_data_dataservice_downloadurl_query_response");
            String downloadUrl = data.get("bill_download_url");

            return new RestResult<>(HttpStatus.SUCCESS, "支付宝" + msgType + "下载成功", downloadUrl);
        } else {
            log.info("支付宝{}接口调用失败", msgType);

            return new RestResult<>(HttpStatus.ERROR, "支付宝" + msgType + "下载失败");
        }
    }
}
