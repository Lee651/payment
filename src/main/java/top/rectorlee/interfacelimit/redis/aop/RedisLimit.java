package top.rectorlee.interfacelimit.redis.aop;

import java.lang.annotation.*;

/**
 * @author Lee
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RedisLimit {
    /**
     * 资源唯一key, 作用是对不同的接口，进行不同的流量控制
     */
    String key() default "";

    /**
     * 最多访问限制次数
     */
    long permitsPerSecond() default 2;

    /**
     * 过期时间，单位秒，默认60
     */
    long expire() default 60;

    /**
     * 未获取到令牌时的提示语
     */
    String msg() default "系统繁忙,请稍后再试.";
}
