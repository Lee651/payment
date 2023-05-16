package top.rectorlee.interfacelimit.redis.exception;

/**
 * @author Lee
 * @description Redis 限流自定义异常
 * @date 2023-04-23  21:11:48
 */
public class RedisLimitException extends RuntimeException{
    public RedisLimitException(String msg) {
        super(msg);
    }
}
