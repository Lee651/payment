package top.rectorlee;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/**
 * @author Lee
 * @description 测试类
 * @date 2023-05-05  14:07:01
 */
@Slf4j
@SpringBootTest
public class AllTest {
    /*@Autowired
    private WxPayConfig wxPayConfig;

    @Test
    public void testWxPayInfo() {
        String privateKeyPath = wxPayConfig.getPrivateKeyPath();
        PrivateKey privatekey = wxPayConfig.getPrivatekey(privateKeyPath);
        System.out.println(privatekey);
    }*/

    @Autowired
    private Environment environment;

    @Test
    public void testAliPay() {
        log.warn("alipay参数为: {}", environment.getProperty("alipay.app-id"));
    }
}
