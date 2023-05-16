package top.rectorlee.config;

import com.alipay.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

/**
 * @author Lee
 * @description 支付宝支付配置类
 * @date 2023-05-08  15:30:10
 */
@Configuration
@PropertySource("classpath:alipay-sandbox.properties")
public class AliPayConfig {
    @Autowired
    private Environment environment;

    @Bean(name = "alipayClient")
    public AlipayClient alipayClient() throws Exception {
        AlipayConfig alipayConfig = new AlipayConfig();

        // 设置网关地址
        alipayConfig.setServerUrl(environment.getProperty("alipay.gateway-url"));
        // 设置应用Id
        alipayConfig.setAppId(environment.getProperty("alipay.app-id"));
        // 设置商户私钥
        alipayConfig.setPrivateKey(environment.getProperty("alipay.merchant-private-key"));
        // 设置支付宝公钥
        alipayConfig.setAlipayPublicKey(environment.getProperty("alipay.alipay-public-key"));
        // 设置请求格式
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        // 设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        // 设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);

        //构造client
        return new DefaultAlipayClient(alipayConfig);
    }
}
