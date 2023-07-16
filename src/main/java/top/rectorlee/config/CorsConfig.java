package top.rectorlee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author Lee
 * @description 跨域配置类: 添加该配置类后所有的接口就不再需要添加@CrossOrigin注解了
 * @date 2023-07-16 14:00:05
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 创建Cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 支持域
        corsConfiguration.addAllowedOrigin("*");
        // 是否发送Cookie
        corsConfiguration.setAllowCredentials(true);
        // 请求方式
        corsConfiguration.addAllowedMethod("*");
        // 添加映射地址
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
