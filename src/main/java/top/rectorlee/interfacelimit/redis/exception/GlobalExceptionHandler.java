package top.rectorlee.interfacelimit.redis.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.rectorlee.utils.HttpStatus;
import top.rectorlee.utils.RestResult;

/**
 * @author Lee
 * @description 全局异常处理类
 * @date 2023-04-23  21:07:59
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Redis限流异常方法
    @ExceptionHandler(RedisLimitException.class)
    public RestResult handlerCustomException(RedisLimitException e) {
        /*e.printStackTrace();*/

        return new RestResult(HttpStatus.ERROR, e.getMessage());
    }


    // 其他异常方法
    @ExceptionHandler(Exception.class)
    public RestResult handlerException(Exception e) {
        e.printStackTrace();

        return new RestResult(HttpStatus.ERROR, "当前系统异常");
    }
}
