package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.CategoryAdminDto;
import com.rabbuy.ecommerce.dto.CategoryInputDto;
import com.rabbuy.ecommerce.dto.CategoryNavDto;
import com.rabbuy.ecommerce.service.CategoryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*; // 导入 JAX-RS 注解
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response; // 导入 JAX-RS Response

import java.util.List;
import java.util.UUID;

// 1. @ApplicationScoped: 使其成为 CDI Bean
// 2. @Path("/category"): 定义基础 URL 路径 (在 /api 之后，即 /api/category)
//    /api 来自 HelloApplication.java
@Path("/category")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON) // 默认返回 JSON
@Consumes(MediaType.APPLICATION_JSON) // 默认接收 JSON
public class CategoryResource {

    @Inject // 3. 注入业务逻辑层
    private CategoryService categoryService;

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

    // TODO: Django 项目中还有 SubCategory 的视图，
    // 它们应该在 SubCategoryService 和 SubCategoryResource 中实现。
}