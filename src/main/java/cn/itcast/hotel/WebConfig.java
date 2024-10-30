package cn.itcast.hotel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsConfig() {
        // 创建CorsConfiguration对象
        CorsConfiguration configuration = new CorsConfiguration();
        // 设置允许的源
        configuration.addAllowedOrigin("*");
        // 设置允许的方法
        configuration.addAllowedMethod("*");
        // 设置允许的头部信息
        configuration.addAllowedHeader("*");
        // 设置允许凭证
        configuration.setAllowCredentials(true);

        // 创建CorsFilter对象
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}