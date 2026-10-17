package com.sky.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * /api 前缀转发过滤器
 * <p>
 * 背景：黑马原版管理端前端打包时 axios 的 baseURL 固定为 "/api"（编译产物已压缩，无法修改源码），
 * 原本依赖 nginx 的 {@code location /api/ { proxy_pass http://localhost:8080/admin/; }} 做前缀替换。
 * 本项目把前端静态资源整合进 Spring Boot 后不再使用 nginx，所以在服务端补一个等价实现。
 * <p>
 * 为什么用 Filter 而不是 HandlerInterceptor：
 * 拦截器的 preHandle 在 handler 已经匹配完毕后才执行，此时改 URI 已经来不及；
 * 必须在 DispatcherServlet 之前包装 request，让 Spring MVC 按新 URI 去匹配 Controller。
 * <p>
 * 因为当前项目只有管理端接口，所以统一转发到 /admin；
 * 后续加上用户端（/user）时，需要在此处按前缀分流。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApiPathRewriteFilter implements Filter {

    private static final String PREFIX = "/api";
    private static final String TARGET_PREFIX = "/admin";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 项目编译级别为 Java 8，此处不能用 instanceof 模式匹配
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String uri = httpRequest.getRequestURI();

        if (uri != null && uri.startsWith(PREFIX + "/")) {
            chain.doFilter(new ApiPathRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 把请求 URI 中的 /api 前缀替换成 /admin，使 /api/employee/login 命中 /admin/employee/login 的 Controller
     */
    private static class ApiPathRequestWrapper extends HttpServletRequestWrapper {

        private final String newUri;

        ApiPathRequestWrapper(HttpServletRequest request) {
            super(request);
            String original = request.getRequestURI();
            this.newUri = original.replaceFirst("^" + PREFIX, TARGET_PREFIX);
            log.info("路径重写: {} -> {}", original, newUri);
        }

        @Override
        public String getRequestURI() {
            return newUri;
        }

        @Override
        public String getServletPath() {
            String servletPath = super.getServletPath();
            if (servletPath != null && servletPath.startsWith(PREFIX)) {
                return servletPath.replaceFirst("^" + PREFIX, TARGET_PREFIX);
            }
            return servletPath;
        }

        @Override
        public StringBuffer getRequestURL() {
            String url = super.getRequestURL().toString();
            // 只替换路径部分，避免把域名里的字符串误替换
            int idx = url.lastIndexOf("/api/");
            if (idx >= 0) {
                return new StringBuffer(url.substring(0, idx) + "/admin/" + url.substring(idx + "/api/".length()));
            }
            return new StringBuffer(url);
        }
    }
}
