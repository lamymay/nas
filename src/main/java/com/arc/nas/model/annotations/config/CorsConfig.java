package com.arc.nas.model.annotations.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.lang.annotation.*;

/**
 * springboot跨域配置
 * https://www.cnblogs.com/nananana/p/8492185.html
 * 使用 XMLHttpRequest 对象发起 HTTP 请求就必须遵守同源策略。
 * 具体而言，Web 应用程序能且只能使用 XMLHttpRequest对象向其加载的源域名发起 HTTP 请求，而不能向任何其它域名发起请求。为了能开发出更强大、更丰富、更安全的Web应用程序，开发人员渴望着在不丢失安全的前提下，Web 应用技术能越来越强大、越来越丰富。比如，可以使用 XMLHttpRequest
 * 发起跨站 HTTP 请求。（这段描述跨域不准确，跨域并非浏览器限制了发起跨站请求，而是跨站请求可以正常发起，但是返回结果被浏览器拦截了。最好的例子是CSRF跨站攻击原理，请求是发送到了后端服务器无论是否跨域！注意：有些浏览器不允许从HTTPS的域跨域访问HTTP，比如Chrome和Firefox，这些浏览器在请求还未发出的时候就会拦截请求，这是一个特例。）
 *
 * @since 2019/3/6 15:52
 */
@Configuration
public class CorsConfig {


    /**
     * 新增一个configration类 或 在Application中加入CorsFilter和CorsConfiguration方法
     *
     * @return CorsConfiguration
     */
    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); // 1允许任何域名使用
        corsConfiguration.addAllowedHeader("*"); // 2允许任何头
        corsConfiguration.addAllowedMethod("*"); // 3允许任何方法（post、get等）
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig()); // 4
        return new CorsFilter(source);
    }

    /**
     * 允许 所有的 跨域
     * EnableCorsConfig 的配置作为注解引入到您需要的项目中去
     *
     * @since 2019/10/23 17:17
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Import(CorsConfig.class)
    @Documented
    public @interface EnableCorsConfig {

    }
}
