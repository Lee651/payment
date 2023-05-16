package top.rectorlee.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.rectorlee.interfacelimit.redis.aop.RedisLimit;
import top.rectorlee.service.ProductService;
import top.rectorlee.utils.HttpStatus;
import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 * @description
 * @date 2023-05-04  17:13:01
 */
@CrossOrigin // 开启前端跨域请求的支持
@Slf4j
@Api(tags = "商品管理接口")
@RestController
@RequestMapping("/api/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @ApiOperation(value = "商品列表")
    @GetMapping("/list")
    // @SentinelResource(value = SystemConstant.PRODUCT_LIST_KEY, fallback = "getDefaultResult")
    @RedisLimit(key = "productList", permitsPerSecond = 2, expire = 1, msg = "redis: 当前请求次数过多，请稍后重试！")
    public RestResult productList() throws Exception {
        return productService.selectProductList();
    }

    public RestResult getDefaultResult(Throwable e) {
        return new RestResult(HttpStatus.ERROR, "sentinel: 当前请求次数过多, 请稍后重试");
    }
}
