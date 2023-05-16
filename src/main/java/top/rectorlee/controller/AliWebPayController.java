package top.rectorlee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.rectorlee.service.AliWebPayService;
import top.rectorlee.utils.RestResult;

import java.util.Map;

/**
 * @author Lee
 * @description 支付宝网页支付接口
 * @date 2023-05-08  17:16:05
 */
@CrossOrigin // 开启跨域支持
@Api(tags = "支付宝网页支付接口")
@Slf4j
@RestController
@RequestMapping("/api/ali-web-pay")
public class AliWebPayController {
    @Autowired
    private AliWebPayService aliWebPayService;

    @ApiOperation(value = "下单")
    @GetMapping("/tradePage/{productId}")
    public RestResult tradePage(@PathVariable("productId") Long productId) throws Exception {
        return aliWebPayService.tradePage(productId);
    }

    @ApiOperation(value = "支付回调")
    @PostMapping("/callBack")
    public RestResult callBack(@RequestParam Map<String, String> parameterMap) throws Exception {
        return aliWebPayService.callBack(parameterMap);
    }

    @ApiOperation(value = "取消订单")
    @GetMapping("/cancel/{orderNo}")
    public RestResult cancelOrder(@PathVariable("orderNo") String orderNo) throws Exception {
        return aliWebPayService.cancelOrder(orderNo);
    }

    @ApiOperation(value = "交易查询")
    @GetMapping("/transactionQuery/{orderNo}")
    public RestResult queryOrder(@PathVariable("orderNo") String orderNo) throws Exception {
        return aliWebPayService.queryOrder(orderNo);
    }

    @ApiOperation(value = "退款")
    @GetMapping("/refund/{orderNo}/{reason}")
    public RestResult refundOrder(@PathVariable("orderNo") String orderNo, @PathVariable("reason") String reason) throws Exception {
        return aliWebPayService.refundOrder(orderNo, reason);
    }

    @ApiOperation(value = "下载交易账单/资金账单")
    @GetMapping("/download/{billDate}/{type}")
    public RestResult downloadBill(@PathVariable("billDate") String billDate, @PathVariable("type") String type) throws Exception {
        return aliWebPayService.downloadBill(billDate, type);
    }
}
