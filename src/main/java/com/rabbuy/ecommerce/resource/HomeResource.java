package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.HomeMessageCountDto;
import com.rabbuy.ecommerce.dto.ProductListDto;
import com.rabbuy.ecommerce.service.OrderService;
import com.rabbuy.ecommerce.service.ProductService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;
import java.util.UUID;

@Path("/home") // 对应 /api/home
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeResource {

    @Inject
    private ProductService productService;

    @Inject
    private OrderService orderService;

    @Inject
    private JsonWebToken jwtPrincipal;

    /**
     * 获取新上架商品
     * @return
     */
    @GET
    @Path("/new")
    public Response getNewProducts() {
        // Django view hardcodes limit to 4
        List<ProductListDto> products = productService.getLatestProducts(4);
        return Response.ok(products).build();
    }

    /**
     * 获取热门商品
     * @return
     */
    @GET
    @Path("/hot")
    public Response getHotProducts() {
        // Django view hardcodes limit to 4
        List<ProductListDto> products = productService.getHotProducts(4);
        return Response.ok(products).build();
    }

    /**
     * 获取订单消息计数（需要认证）
     * @return
     */
    @GET
    @Path("/message")
    @RolesAllowed({"admin", "customer"}) // (token_required)
    public Response getMessageCounts() {
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());
        HomeMessageCountDto counts = orderService.getMessageCountsByUserId(currentUserId);
        return Response.ok(counts).build();
    }
}