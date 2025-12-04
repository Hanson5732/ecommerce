package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.ApiResponseDto;
import com.rabbuy.ecommerce.dto.CartItem;
import com.rabbuy.ecommerce.dto.CartResponseDto;
import com.rabbuy.ecommerce.service.CartService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

import java.util.List;

@Path("/cart")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    private CartService cartService;

    @Context
    private SecurityContext securityContext;

    /**
     * 获取当前用户的购物车详情
     * 对应 Django: path('', views.get_cart_view, name='getCart')
     * 访问: GET /api/cart
     */
    @GET
    public Response getCart() {
        // 从 JWT 获取用户 ID，而不是像 Django 那样从查询参数获取
        String currentUserId = securityContext.getUserPrincipal().getName();

        // NotFoundException 会被 GlobalExceptionMapper 自动捕获
        CartResponseDto cart = cartService.getCartByUserId(currentUserId);

        return Response.ok(ApiResponseDto.success(cart)).build();
    }

    /**
     * 保存（覆盖）当前用户的购物车
     * @param cartItems
     * @return
     */
    @POST
    @Path("/save")
    public Response saveCart(List<CartItem> cartItems) {
        String currentUserId = securityContext.getUserPrincipal().getName();

        if (cartItems == null) {
            throw new WebApplicationException("Request body (list of products) is required.", Response.Status.BAD_REQUEST);
        }

        // NotFoundException 会被 GlobalExceptionMapper 自动捕获
        cartService.saveCart(currentUserId, cartItems);

        // 在 JAX-RS 中，我们通常返回 200 OK 或 204 No Content
        return Response.ok(ApiResponseDto.success()).build();
    }
}