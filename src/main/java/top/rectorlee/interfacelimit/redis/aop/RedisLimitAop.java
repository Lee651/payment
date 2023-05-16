package top.rectorlee.interfacelimit.redis.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import top.rectorlee.interfacelimit.redis.exception.RedisLimitException;
import wiremock.org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lee
 * @description Redis 限流切面类
 * @date 2023-04-23  21:19:15
 */
@Slf4j
@Aspect
@Component
public class RedisLimitAop {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> redisScript;

    @Pointcut("@annotation(top.rectorlee.interfacelimit.redis.aop.RedisLimit)")
    private void check() {

    }

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redisLimit.lua")));
    }

    @Before("check()")
    public void before(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 拿到RedisLimit注解，如果存在则说明需要限流
        RedisLimit redisLimit = method.getAnnotation(RedisLimit.class);

        if (redisLimit != null) {
            // 获取RedisLimit注解上的值
            String key = redisLimit.key();
            String className = method.getDeclaringClass().getName();
            String name = method.getName();

            log.info(key + " | " + className + " | " + name);

            if (StringUtils.isEmpty(key)) {
                throw new RedisLimitException("key cannot be null");
            }

            long limit = redisLimit.permitsPerSecond();

            long expire = redisLimit.expire();

            List<String> keys = new ArrayList<>();
            keys.add(key);

            // Redis中该接口的访问次数(count=0表示访问次数大于2次)
            Long count = stringRedisTemplate.execute(redisScript, keys, String.valueOf(limit), String.valueOf(expire));

            log.info("Access try count is {} for key={}", count, key);

            if (count != null && count == 0) {
                log.debug("获取key失败，key为{}", key);
                throw new RedisLimitException(redisLimit.msg());
            }
        }
    }
}
