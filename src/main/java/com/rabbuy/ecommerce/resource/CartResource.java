package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.CartItem;
import com.rabbuy.ecommerce.dto.CartResponseDto;
import com.rabbuy.ecommerce.service.CartService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/cart")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "customer"}) // 保护此资源下的所有端点
public class CartResource {

    @Inject
    private CartService cartService;

    @Inject // 注入已认证的 JWT
    private JsonWebToken jwtPrincipal;

    /**
     * 获取当前用户的购物车详情
     * 对应 Django: path('', views.get_cart_view, name='getCart')
     * 访问: GET /api/cart
     */
    @GET
    public Response getCart() {
        // 从 JWT 获取用户 ID，而不是像 Django 那样从查询参数获取
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());

        // NotFoundException 会被 GlobalExceptionMapper 自动捕获
        CartResponseDto cart = cartService.getCartByUserId(currentUserId);

        return Response.ok(cart).build();
    }

    /**
     * 保存（覆盖）当前用户的购物车
     * 对应 Django: path('save/', views.save_cart_view, name="saveCart")
     * 访问: POST /api/cart/save
     * * @param cartItems 购物车项目列表，例如: [{"id": "uuid-...", "count": 2}]
     * @return 200 OK
     */
    @POST
    @Path("/save")
    public Response saveCart(List<CartItem> cartItems) {
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());

        if (cartItems == null) {
            throw new WebApplicationException("Request body (list of products) is required.", Response.Status.BAD_REQUEST);
        }

        // NotFoundException 会被 GlobalExceptionMapper 自动捕获
        cartService.saveCart(currentUserId, cartItems);

        // Django 的 save_cart_view 返回 Result.success()
        // 在 JAX-RS 中，我们通常返回 200 OK 或 204 No Content
        return Response.ok().build();
    }

    // Django 中的 add_cart_view 似乎是用于创建购物车的内部逻辑 (被注册逻辑调用)，
    // 在 Java 中，这个逻辑已经在 UserService.registerUser 中通过 CartService.createEmptyCart 实现。
    // 因此我们不需要为 /add 单独暴露 API。
}