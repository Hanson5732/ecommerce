package com.rabbuy.ecommerce.filter;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import java.io.IOException;
import java.security.Key;
import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.IOException;

@Provider // 告诉 JAX-RS 这是一个全局过滤器
@Priority(Priorities.AUTHENTICATION) // 确保它在授权 (@RolesAllowed) 之前运行
@ApplicationScoped
public class JwtAuthFilter implements ContainerRequestFilter {

    @Inject
    @ConfigProperty(name = "jwt.secret.key")
    private String secretKeyString; // 注入与 TokenServiceImpl *相同* 的密钥

    @Inject
    @ConfigProperty(name = "jwt.issuer")
    private String issuer; // 注入与 TokenServiceImpl *相同* 的签发者

    private Key signingKey = null;
    private JwtConsumer jwtConsumer = null;

    /**
     * 初始化 HmacKey，与 TokenServiceImpl 完全一致
     */
    private Key getKey() {
        if (this.signingKey == null) {
            byte[] secretBytes = Base64.getDecoder().decode(secretKeyString);
            this.signingKey = new HmacKey(secretBytes);
        }
        return this.signingKey;
    }

    /**
     * 初始化 JWT 验证器
     */
    private JwtConsumer getJwtConsumer() {
        if (this.jwtConsumer == null) {
            AlgorithmConstraints algConstraints = new AlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    AlgorithmIdentifiers.HMAC_SHA256
            );

            this.jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setExpectedIssuer(this.issuer)
                    .setExpectedAudience("ecommerce-clients")
                    .setVerificationKey(getKey())
                    .setJwsAlgorithmConstraints(algConstraints)
                    .build();
        }
        return this.jwtConsumer;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // 1. 放行公共端点 (例如登录、注册)
        if (isPublicEndpoint(path, requestContext.getMethod())) {
            return;
        }

        // 2. 获取 Authorization Header
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // 3. 检查 Header 是否存在且格式正确
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWith401(requestContext, "Authorization header must be provided");
            return;
        }

        // 4. 提取 Token 字符串
        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            // 5. [核心] 使用我们的 HS256 验证器验证 Token
            JwtClaims claims = getJwtConsumer().processToClaims(token);

            // 6. [重要] 验证成功，构建 SecurityContext
            // 这一步使得 @RolesAllowed 和 @Inject JsonWebToken 能够继续工作
            String userId = claims.getSubject();
            List<String> rolesList = claims.getStringListClaimValue("groups");
            Set<String> roles = rolesList.stream().collect(Collectors.toSet());

            SecurityContext originalContext = requestContext.getSecurityContext();
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    // Principal 的 name 就是 User ID
                    return () -> userId;
                }
                @Override
                public boolean isUserInRole(String role) {
                    return roles.contains(role);
                }
                @Override
                public boolean isSecure() {
                    return originalContext.isSecure();
                }
                @Override
                public String getAuthenticationScheme() {
                    return "JWT-BEARER"; // 
                }
            });

        } catch (InvalidJwtException e) {
            // 7. 验证失败 (签名错误、过期、格式错误等)
            abortWith401(requestContext, "JWT Token is invalid or expired");
        } catch (Exception e) {
            abortWith401(requestContext, "An error occurred during authentication");
        }
    }

    private void abortWith401(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"code\": 0, \"msg\": \"" + message + "\"}")
                        .type(jakarta.ws.rs.core.MediaType.APPLICATION_JSON)
                        .build()
        );
    }

    private boolean isPublicEndpoint(String path, String method) {
        // 移除可能存在的末尾斜杠，统一路径格式
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // 1. 用户认证相关 (UserResource)
        if (path.equals("user/login") ||
                path.equals("user/register") ||
                path.equals("user/token-refresh")) {
            return true;
        }

        // 2. 首页数据 (HomeResource)
        if (path.equals("home/new") ||
                path.equals("home/hot") ||
                path.equals("home/products") ||
                path.equals("home/recommend")) {
            return true;
        }

        // 3. 商品浏览相关 (ProductResource)
        if (path.startsWith("product/detail") ||
                path.startsWith("product/search") ||
                path.startsWith("product/recommend")) {
            return true;
        }

        // 4. 分类导航相关 (CategoryResource)
        if (path.equals("category/nav") ||
                path.startsWith("category/sub/product") ||
                path.startsWith("category/sub/filter") ||
                path.equals("category/all")) {
            return true;
        }

        return false;
    }
}