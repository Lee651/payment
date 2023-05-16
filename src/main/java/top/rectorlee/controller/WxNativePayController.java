package top.rectorlee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.rectorlee.service.WxNativePayService;
import top.rectorlee.utils.RestResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lee
 * @description 微信Native接口
 * @date 2023-05-05  15:40:18
 */
@CrossOrigin // 开启前端跨域请求的支持
@Slf4j
@Api(tags = "微信Native支付接口")
@RestController
@RequestMapping("/api/wx-native-pay")
public class WxNativePayController {
    @Autowired
    private WxNativePayService wxNativePayService;

    @ApiOperation(value = "下单")
    @GetMapping("/native/{productId}")
    public RestResult order(@PathVariable("productId") Long productId) throws Exception {
        return wxNativePayService.order(productId);
    }

    @ApiOperation(value = "支付回调")
    @PostMapping("/callBack")
    public RestResult callBack(HttpServletRequest request, HttpServletResponse response) {
        return wxNativePayService.callBack(request, response);
    }

    @ApiOperation(value = "取消订单")
    @GetMapping("/cancel/{orderNo}")
    public RestResult cancelOrder(@PathVariable("orderNo") String orderNo) throws Exception {
        return wxNativePayService.cancelOrder(orderNo);
    }

    @ApiOperation(value = "交易查询")
    @GetMapping("/query/{orderNo}")
    public RestResult queryOrder(@PathVariable("orderNo") String orderNo) throws Exception {
        return wxNativePayService.queryOrder(orderNo);
    }

    @ApiOperation(value = "退款")
    @GetMapping("/refund/{orderNo}/{reason}")
    public RestResult refund(@PathVariable("orderNo") String orderNo, @PathVariable("reason") String reason) throws Exception {
        return wxNativePayService.refund(orderNo, reason);
    }

    @ApiOperation(value = "退款回调")
    @PostMapping("/refundCallBack")
    public RestResult refundCallBack(HttpServletRequest request, HttpServletResponse response) {
        return wxNativePayService.refundCallBack(request, response);
    }

    @ApiOperation(value = "下载交易账单/资金账单")
    @GetMapping("/download/{billDate}/{type}")
    public RestResult downloadBill(@PathVariable("billDate") String billDate, @PathVariable("type") String type) throws Exception {
        return wxNativePayService.downloadBill(billDate, type);
    }
}
