package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.ApiResponseDto;
import com.rabbuy.ecommerce.dto.HomeMessageCountDto;
import com.rabbuy.ecommerce.dto.HomeProductResponseDto;
import com.rabbuy.ecommerce.dto.ProductListDto;
import com.rabbuy.ecommerce.service.OrderService;
import com.rabbuy.ecommerce.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;
import com.rabbuy.ecommerce.dto.RecommendCategoryResponseDto;

import java.util.List;


@Path("/home") // 对应 /api/home
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeResource {

    @Inject
    private ProductService productService;

    @Inject
    private OrderService orderService;

    @Context
    private SecurityContext securityContext;

    /**
     * 获取新上架商品
     * @return
     */
    @GET
    @Path("/new")
    public Response getNewProducts() {
        // Django view hardcodes limit to 4
        List<ProductListDto> products = productService.getLatestProducts(4);
        return Response.ok(ApiResponseDto.success(products)).build();
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
        return Response.ok(ApiResponseDto.success(products)).build();
    }

    /**
     * 获取首页商品（随机分类）
     * @return
     */
    @GET
    @Path("/products")
    public Response getHomeProducts() {
        List<HomeProductResponseDto> products = productService.getHomeProducts();
        return Response.ok(ApiResponseDto.success(products)).build();
    }

    /**
     * 获取首页按分类推荐
     * @return
     */
    @GET
    @Path("/recommend")
    public Response getRecommendCategories() {
        List<RecommendCategoryResponseDto> data = productService.getRecommendCategories();
        return Response.ok(ApiResponseDto.success(data)).build();
    }

    /**
     * 获取订单消息计数（需要认证）
     * @return
     */
    @GET
    @Path("/message")
    public Response getMessageCounts() {
        String currentUserId = securityContext.getUserPrincipal().getName();
        HomeMessageCountDto counts = orderService.getMessageCountsByUserId(currentUserId);
        return Response.ok(ApiResponseDto.success(counts)).build();
    }
}