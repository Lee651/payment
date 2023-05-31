package top.rectorlee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.rectorlee.service.OrderInfoService;
import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 * @description
 * @date 2023-05-07  14:23:40
 */
@CrossOrigin // 开启前端跨域请求的支持
@Slf4j
@Api(tags = "订单管理接口")
@RestController
@RequestMapping("/api/order")
public class OrderInfoController {
    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "根据订单号查询订单信息")
    @GetMapping("/orderStatus/{orderNo}")
    public RestResult selectOrderStatus(@PathVariable("orderNo") String orderNo) {
        return orderInfoService.selectOrderStatusByOrderNo(orderNo);
    }

    @ApiOperation(value = "订单列表")
    @GetMapping("/list")
    public RestResult selectOrderList() throws Exception {
        return orderInfoService.selectOrderList();
    }
}
