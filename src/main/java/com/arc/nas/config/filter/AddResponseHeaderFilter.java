package com.arc.nas.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AddResponseHeaderFilter extends org.springframework.web.filter.OncePerRequestFilter {

    /**
     * POM版本
     */
    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        httpServletResponse.addHeader("version", applicationVersion);
//        String curOrigin = httpServletRequest.getHeader("Origin");
//        httpServletResponse.setHeader("Access-Control-Allow-Origin", curOrigin == null ? "true" : curOrigin);
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
//        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS,TRACE");
        httpServletResponse.setHeader("Access-Control-Max-Age", "3600");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "access-control-allow-origin, x-custom-header,authority, content-type, version-info, X-Requested-With");

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}


