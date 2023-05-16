package top.rectorlee.interfacelimit.others;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lee
 * @description 固定窗口算法限流
 * @date 2023-05-11  10:28:10
 */
@Slf4j
public class FixedWindowRateLimiter {
    // 时间窗口大小, 单位毫秒
    private long windowsSize;

    // 允许通过的请求数
    private int maxRequestCount;

    // 当前窗口通过的请求计数
    private AtomicInteger count = new AtomicInteger(0);

    // 窗口右边界
    private long windowBorder;

    public FixedWindowRateLimiter(long windowsSize, int maxRequestCount) {
        this.windowsSize = windowsSize;
        this.maxRequestCount = maxRequestCount;

        windowBorder = System.currentTimeMillis() + windowsSize;
    }

    public synchronized boolean tryAcquire() {
        long currentTime = System.currentTimeMillis();

        if (windowBorder < currentTime) {
            log.info("window reset");

            do {
                windowBorder += windowsSize;
            } while (windowBorder < currentTime);

            count = new AtomicInteger(0);
        }

        if (count.intValue() < maxRequestCount) {
            count.incrementAndGet();
            log.info("tryAcquire success");

            return true;
        } else {
            log.info("tryAcquire fail");

            return false;
        }
    }
}
