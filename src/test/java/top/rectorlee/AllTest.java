package top.rectorlee;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
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
    @Autowired
    private Environment environment;

    @Autowired
    private StringEncryptor stringEncryptor;

    @Test
    public void testAliPay() {
        log.warn("alipay参数为: {}", environment.getProperty("alipay.app-id"));
    }

    @Test
    public void testJasypt() {
        String root = stringEncryptor.encrypt("jdbc:mysql://localhost:3306/payment?characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai");
        String decrypt = stringEncryptor.decrypt(root);
        System.out.println("明文为: " + decrypt);
        System.out.println("密文为: " + root);
    }
}
