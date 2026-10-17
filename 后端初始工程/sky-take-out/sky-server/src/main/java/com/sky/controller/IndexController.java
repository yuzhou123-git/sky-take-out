package com.sky.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端首页控制器
 * <p>
 * 把黑马管理端前端（编译产物）整合进 Spring Boot 后，静态资源统一放在
 * {@code sky-server/src/main/resources/static/} 下，访问 /index.html 正常，
 * 但直接访问根路径 / 会 404 —— 因为 WebMvcConfiguration 继承 WebMvcConfigurationSupport 后
 * 关闭了 Spring Boot 默认的欢迎页（welcome page）自动配置。
 * <p>
 * 这里用一个最小的 Controller 把 / 映射到 static/index.html。
 * 也可以改用 addViewControllers 的 forward 写法，但那样需要额外注册 InternalResourceViewResolver，
 * 对初学者来说不如直接返回 Resource 直观。
 */
@RestController
public class IndexController {

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> index() {
        Resource resource = new ClassPathResource("static/index.html");
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8))
                .body(resource);
    }
}
