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


@Path("/category")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
        return Response.ok(ApiResponseDto.success(categories)).build();
    }

    @GET
    @Path("/all") // 对应 /api/category/all
    public Response getCategoryDetails(@QueryParam("id") String id) {
        if (id == null) {
            throw new WebApplicationException("Query parameter 'id' is required.", Response.Status.BAD_REQUEST);
        }

        // 调用 Service 层方法
        CategoryDetailDto detailDto = categoryService.getCategoryDetails(id);

        return Response.ok(ApiResponseDto.success(detailDto)).build();
    }

    /**
     * 获取管理员后台分类列表（带分页）
     * 对应: path('admin/list/', ...)
     * 访问: GET /api/category/admin/list?page=1&pageSize=10
     */
    @GET
    @Path("/admin/list")
    public Response getAdminCategories(@QueryParam("page") @DefaultValue("1") int page,
                                       @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        // 修改：调用 Service 获取 PaginatedResult
        PaginatedResult<CategoryAdminDto> result = categoryService.getAdminCategories(page, pageSize);

        // 返回：前端将接收到 { code: 1, data: { data: [...], totalItems: 100, ... } }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    @GET
    @Path("/list")
    public Response getAllCategoriesSimple() {
        // 复用逻辑，取前 1000 个，或者你可以专门写一个 findAll 不分页的 Service 方法
        PaginatedResult<CategoryAdminDto> result = categoryService.getAdminCategories(1, 1000);
        // 如果前端下拉框只需要 list，可以返回 result.data()
        return Response.ok(ApiResponseDto.success(result.data())).build();
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
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(newCategory)).build();
    }

    /**
     * 更新分类
     * 对应: path('update/', ...)
     * 访问: PUT /api/category/update/{id}
     */
    @PUT
    @Path("/update/{id}") // @PathParam 用于从 URL 路径获取参数
    public Response updateCategory(@PathParam("id") String id, CategoryInputDto categoryDto) {
        CategoryAdminDto updatedCategory = categoryService.updateCategory(id, categoryDto);
        return Response.ok(ApiResponseDto.success(updatedCategory)).build();
    }

    /**
     * 删除分类
     * 对应: path('delete/', ...)
     * 访问: DELETE /api/category/delete/{id}
     */
    @DELETE
    @Path("/delete/{id}")
    public Response deleteCategory(@PathParam("id") String id) {
        categoryService.deleteCategory(id);
        return Response.ok(ApiResponseDto.success("Category deleted successfully")).build();
    }

    /**
     * 获取二级分类导航（详情）
     * @param id
     * @return
     */
    @GET
    @Path("/sub/filter/{id}")
    public Response getSubCategoryFilter(@PathParam("id") String id) {
        SubCategoryDto dto = subCategoryService.getSubCategoryDetails(id);
        return Response.ok(ApiResponseDto.success(dto)).build();
    }

    /**
     * 获取二级分类下的商品列表
     * @param subCategoryId
     * @param page
     * @param pageSize
     * @param sortField
     * @param minPrice
     * @param maxPrice
     * @return
     */
    @GET
    @Path("/sub/product")
    public Response getSubCategoryProducts(
            @QueryParam("subCategoryId") String subCategoryId,
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
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * 获取所有二级分类列表 (用于管理后台)
     * @return
     */
    @GET
    @Path("/sub/list")
    public Response getAllSubCategories() {
        List<SubCategoryDto> dtos = subCategoryService.getAllSubCategories();
        return Response.ok(ApiResponseDto.success(dtos)).build();
    }

    /**
     * 分页获取所有二级分类 (用于管理后台)
     * @param page
     * @param pageSize
     * @return
     */
    @GET
    @Path("/admin/sub/list")
    public Response getAdminSubCategories(
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        PaginatedResult<SubCategoryDto> result = subCategoryService.getAdminSubCategories(page, pageSize);
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * 添加二级分类
     * @param dto
     * @return
     */
    @POST
    @Path("/sub/add")
    public Response addSubCategory(SubCategoryInputDto dto) {
        SubCategoryDto newDto = subCategoryService.addSubCategory(dto);
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(newDto)).build();
    }

    /**
     * 更新二级分类
     * @param id
     * @param dto
     * @return
     */
    @PUT
    @Path("/sub/update/{id}")
    public Response updateSubCategory(@PathParam("id") String id, SubCategoryInputDto dto) {
        SubCategoryDto updatedDto = subCategoryService.updateSubCategory(id, dto);
        return Response.ok(ApiResponseDto.success(updatedDto)).build();
    }

    /**
     * 删除二级分类
     * @param id
     * @return
     */
    @DELETE
    @Path("/sub/delete/{id}")
    public Response deleteSubCategory(@PathParam("id") String id) {
        subCategoryService.deleteSubCategory(id);
        return Response.ok(ApiResponseDto.success("Subcategory deleted successfully")).build();
    }
}