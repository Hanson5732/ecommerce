package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.*;
import jakarta.annotation.security.RolesAllowed;
import org.eclipse.microprofile.jwt.JsonWebToken;
import com.rabbuy.ecommerce.service.ProductService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/product") // 对应 /api/product
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    private ProductService productService;

    @Inject
    private JsonWebToken jwtPrincipal;

    /**
     * 获取商品详情
     * 访问: GET /api/product/detail/{id}
     *
     */
    @GET
    @Path("/detail/{id}")
    public Response getProductDetail(@PathParam("id") UUID productId) {
        // NotFoundException 会被 GlobalExceptionMapper 自动捕获
        ProductDetailDto productDetail = productService.getProductDetails(productId);
        return Response.ok(productDetail).build();
    }

    /**
     * 获取商品可用状态
     * 对应 Django: path('detail/status/', ...)
     * 访问: GET /api/product/detail/status?id=...
     *
     */
    @GET
    @Path("/detail/status")
    public Response getProductStatus(@QueryParam("id") UUID productId) {
        if (productId == null) {
            //
            throw new WebApplicationException("Missing product ID parameter", Response.Status.BAD_REQUEST);
        }
        ProductStatusDto status = productService.getProductStatus(productId);
        return Response.ok(status).build();
    }

    /**
     * 搜索商品（带分页和排序）
     * 对应 Django: path('search/', ...)
     * 访问: GET /api/product/search?q=...&category=...&page=...
     *
     */
    @GET
    @Path("/search")
    public Response searchProducts(
            @QueryParam("q") @DefaultValue("") String keyword,
            @QueryParam("category") UUID categoryId, //
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
            @QueryParam("sortField") @DefaultValue("default") String sortField,
            @QueryParam("sortMin") BigDecimal minPrice,
            @QueryParam("sortMax") BigDecimal maxPrice) {

        // 将所有查询参数封装到 DTO 中
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                keyword, categoryId, minPrice, maxPrice, sortField, page, pageSize
        );

        PaginatedResult<ProductListDto> results = productService.searchProducts(criteria);
        return Response.ok(results).build();
    }

    /**
     * 获取商品推荐
     * 对应 Django: path('recommend/', ...)
     * 访问: GET /api/product/recommend?id=...&name=...
     *
     */
    @GET
    @Path("/recommend")
    public Response getRecommendations(
            @QueryParam("id") UUID productId,
            @QueryParam("name") String name) {

        if (productId == null || name == null) {
            throw new WebApplicationException("Query parameters 'id' and 'name' are required", Response.Status.BAD_REQUEST);
        }

        List<ProductListDto> recommendations = productService.getProductRecommendations(productId, name, 4); // Django 限制 4 个
        return Response.ok(recommendations).build();
    }

    /**
     * 【新】管理员：获取商品库存状态
     * 对应 Django: path('stock-count/', ...)
     * 访问: GET /api/product/stock-count
     *
     */
    @GET
    @Path("/stock-count")
    @RolesAllowed("admin") //
    public Response getStockStatus() {
        ProductStockStatus status = productService.getProductStockStatus();
        return Response.ok(status).build();
    }

    /**
     * 【新】管理员：获取商品列表（分页）
     * 对应 Django: path('list/', ...)
     * 访问: GET /api/product/list?q=...&page=...
     *
     */
    @GET
    @Path("/list")
    @RolesAllowed("admin") //
    public Response getAdminProductList(
            @QueryParam("q") @DefaultValue("") String query,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        PaginatedResult<ProductAdminListDto> result = productService.getAdminProductList(query, page, pageSize);

        // 匹配 Django 的 'get_product_view' 响应格式
        Map<String, Object> response = new HashMap<>();
        response.put("products", result.data());
        response.put("total", result.totalItems());

        return Response.ok(response).build();
    }

    /**
     * 【新】管理员：获取单个商品详情（不过滤状态）
     * 对应 Django: path('admin/<uuid:pk>/', ...)
     * 访问: GET /api/product/admin/{id}
     *
     */
    @GET
    @Path("/admin/{id}")
    @RolesAllowed("admin") //
    public Response getAdminProductDetail(@PathParam("id") UUID productId) {
        ProductDetailDto productDetail = productService.getAdminProductDetails(productId);
        return Response.ok(productDetail).build();
    }

    /**
     * 【新】管理员：添加新商品
     * 对应 Django: path('add/', ...)
     * 访问: POST /api/product/add
     *
     */
    @POST
    @Path("/add")
    @RolesAllowed("admin") //
    public Response addProduct(ProductAdminInputDto dto) {
        ProductDetailDto newProduct = productService.addProduct(dto);
        // Django 视图 返回 {'id': ...}
        Map<String, UUID> response = new HashMap<>();
        response.put("id", newProduct.id());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    /**
     * 【新】管理员：更新商品
     * 对应 Django: path('update/<str:id>/', ...)
     * 访问: PUT /api/product/update/{id}
     *
     */
    @PUT
    @Path("/update/{id}")
    @RolesAllowed("admin") //
    public Response updateProduct(@PathParam("id") UUID productId, ProductAdminInputDto dto) {
        productService.updateProduct(productId, dto);
        return Response.ok().build();
    }

    /**
     * 【新】管理员：逻辑删除商品
     * 对应 Django: path('delete/<str:id>/', ...)
     * 访问: DELETE /api/product/delete/{id}
     * (注意: Django 使用 PATCH, 但 DELETE 更符合 RESTful 语义)
     */
    @DELETE // 使用 DELETE 动词
    @Path("/delete/{id}")
    @RolesAllowed("admin") //
    public Response deleteProduct(@PathParam("id") UUID productId) {
        productService.deleteProduct(productId);
        return Response.ok().build();
    }
}