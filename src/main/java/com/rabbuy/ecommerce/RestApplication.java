package com.rabbuy.ecommerce;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api") // 设置所有 API 的基础路径为 /api
@ApplicationScoped // 使其成为 CDI Bean
@DeclareRoles({"admin", "customer"}) // 声明应用中使用的角色 (对应 'groups' claim)
public class RestApplication extends Application {
    // JAX-RS 会自动发现你项目中的所有 @Path (Resource) 类和 @Provider (ExceptionMapper) 类
}