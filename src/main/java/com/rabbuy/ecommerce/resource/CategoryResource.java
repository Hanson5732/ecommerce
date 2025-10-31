package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.service.SubCategoryService;
import jakarta.annotation.security.RolesAllowed;
import com.rabbuy.ecommerce.service.CategoryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Path("/category")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON) // 默认返回 JSON
@Consumes(MediaType.APPLICATION_JSON) // 默认接收 JSON
public class CategoryResource {

    @Inject
    private CategoryService categoryService;

    @Inject
    private SubCategoryService subCategoryService;

    /**
     * 获取导航栏分类
     * 对应: path('nav/', ...)
     * 访问: GET /api/category/nav
     */
    @GET
    @Path("/nav")
    public Response getNavigationCategories() {
        List<CategoryNavDto> categories = categoryService.getNavigationCategories();
        return Response.ok(categories).build(); // 返回 200 OK 和列表
    }

    /**
     * 获取管理员后台分类列表（带分页）
     * 对应: path('admin/list/', ...)
     * 访问: GET /api/category/admin/list?page=1&pageSize=10
     */
    @GET
    @Path("/admin/list")
    // @QueryParam 用于从 URL query 获取参数
    public Response getAdminCategories(@QueryParam("page") @DefaultValue("1") int page,
                                       @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        // TODO: 当前 Service 层未完全实现分页，但 API 接口已准备好
        List<CategoryAdminDto> categories = categoryService.getAdminCategories(page, pageSize);
        return Response.ok(categories).build();
    }

    /**
     * 添加新分类
     * 对应: path('add/', ...)
     * 访问: POST /api/category/add
     */
    @POST
    @Path("/add")
    // JAX-RS 自动将请求的 JSON body 转换为 CategoryInputDto 对象
    public Response addCategory(CategoryInputDto categoryDto) {
        // Service 层会处理异常，ExceptionMapper 会捕获它们
        CategoryAdminDto newCategory = categoryService.addCategory(categoryDto);
        // 返回 201 Created 状态和新创建的资源
        return Response.status(Response.Status.CREATED).entity(newCategory).build();
    }

    /**
     * 更新分类
     * 对应: path('update/', ...)
     * 访问: PUT /api/category/update/{id}
     */
    @PUT
    @Path("/update/{id}") // @PathParam 用于从 URL 路径获取参数
    public Response updateCategory(@PathParam("id") UUID id, CategoryInputDto categoryDto) {
        CategoryAdminDto updatedCategory = categoryService.updateCategory(id, categoryDto);
        return Response.ok(updatedCategory).build(); // 返回 200 OK 和更新后的资源
    }

    /**
     * 删除分类
     * 对应: path('delete/', ...)
     * 访问: DELETE /api/category/delete/{id}
     */
    @DELETE
    @Path("/delete/{id}")
    public Response deleteCategory(@PathParam("id") UUID id) {
        categoryService.deleteCategory(id);
        return Response.noContent().build(); // 返回 204 No Content
    }

    /**
     * 【新】获取二级分类导航（详情）
     * 对应: path('sub/filter/<str:id>/', ...)
     * 访问: GET /api/category/sub/filter/{id}
     */
    @GET
    @Path("/sub/filter/{id}")
    public Response getSubCategoryFilter(@PathParam("id") UUID id) {
        SubCategoryDto dto = subCategoryService.getSubCategoryDetails(id);
        return Response.ok(dto).build();
    }

    /**
     * 【新】获取二级分类下的商品列表
     * 对应: path('sub/product/', ...)
     * 访问: GET /api/category/sub/product?subCategoryId=...&page=...
     */
    @GET
    @Path("/sub/product")
    public Response getSubCategoryProducts(
            @QueryParam("subCategoryId") UUID subCategoryId,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @QueryParam("sortField") @DefaultValue("default") String sortField,
            @QueryParam("sortMin") BigDecimal minPrice,
            @QueryParam("sortMax") BigDecimal maxPrice) {

        if (subCategoryId == null) {
            throw new WebApplicationException("Query parameter 'subCategoryId' is required.", Response.Status.BAD_REQUEST);
        }

        PaginatedResult<SubCategoryProductDto> result = subCategoryService.getSubCategoryProducts(
                subCategoryId, minPrice, maxPrice, sortField, page, pageSize
        );
        return Response.ok(result).build();
    }

    /**
     * 【新】获取所有二级分类列表 (用于管理后台)
     * 对应: path('sub/list/', ...)
     * 访问: GET /api/category/sub/list
     */
    @GET
    @Path("/sub/list")
    @RolesAllowed("admin") //
    public Response getAllSubCategories() {
        List<SubCategoryDto> dtos = subCategoryService.getAllSubCategories();
        return Response.ok(dtos).build();
    }

    /**
     * 【新】分页获取所有二级分类 (用于管理后台)
     * 对应: path('admin/sub/list/', ...)
     * 访问: GET /api/category/admin/sub/list
     */
    @GET
    @Path("/admin/sub/list")
    @RolesAllowed("admin") //
    public Response getAdminSubCategories(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        PaginatedResult<SubCategoryDto> result = subCategoryService.getAdminSubCategories(page, pageSize);
        return Response.ok(result).build();
    }

    /**
     * 【新】添加二级分类
     * 对应: path('sub/add/', ...)
     * 访问: POST /api/category/sub/add
     */
    @POST
    @Path("/sub/add")
    @RolesAllowed("admin") //
    public Response addSubCategory(SubCategoryInputDto dto) {
        SubCategoryDto newDto = subCategoryService.addSubCategory(dto);
        return Response.status(Response.Status.CREATED).entity(newDto).build();
    }

    /**
     * 【新】更新二级分类
     * 对应: path('sub/update/', ...)
     * 访问: PUT /api/category/sub/update/{id}
     * (注意: Django 使用 PUT /sub/update/ 并从 body 获取 id，
     * 我们使用更 RESTful 的 PUT /sub/update/{id})
     */
    @PUT
    @Path("/sub/update/{id}")
    @RolesAllowed("admin") //
    public Response updateSubCategory(@PathParam("id") UUID id, SubCategoryInputDto dto) {
        SubCategoryDto updatedDto = subCategoryService.updateSubCategory(id, dto);
        return Response.ok(updatedDto).build();
    }

    /**
     * 【新】删除二级分类
     * 对应: path('sub/delete/', ...)
     * 访问: DELETE /api/category/sub/delete/{id}
     * (注意: Django 使用 DELETE /sub/delete/ 并从 body 获取 id，
     * 我们使用更 RESTful 的 DELETE /sub/delete/{id})
     */
    @DELETE
    @Path("/sub/delete/{id}")
    @RolesAllowed("admin") //
    public Response deleteSubCategory(@PathParam("id") UUID id) {
        subCategoryService.deleteSubCategory(id);
        return Response.noContent().build();
    }
}