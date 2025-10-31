package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.entity.OrderItem;
import com.rabbuy.ecommerce.service.OrderService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/order")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"admin", "customer"})
public class OrderResource {

    @Inject
    private OrderService orderService;

    @Inject
    private JsonWebToken jwtPrincipal;

    /**
     * 创建订单
     * @param createDto
     * @return
     */
    @POST
    @Path("/create")
    public Response createOrder(OrderCreateDto createDto) {
        // 异常 (NotFound, IllegalState) 将被 GlobalExceptionMapper 捕获
        OrderCreatedDto createdOrder = orderService.createOrder(createDto);
        return Response.status(Response.Status.CREATED).entity(createdOrder).build();
    }

    /**
     * 获取订单详情
     * @param orderId
     * @return
     */
    @GET
    public Response getOrderDetails(@QueryParam("id") UUID orderId) {
        if (orderId == null) {
            throw new WebApplicationException("Query parameter 'id' is required.", Response.Status.BAD_REQUEST);
        }
        OrderDetailResponseDto orderDetails = orderService.getOrderDetails(orderId);
        return Response.ok(orderDetails).build();
    }

    /**
     * 更新订单（例如支付后更新状态）
     * @param updateDto
     * @return
     */
    @PUT
    @Path("/update")
    public Response updateOrder(OrderUpdateDto updateDto) {
        OrderDetailResponseDto updatedOrder = orderService.updateOrder(updateDto);
        return Response.ok(updatedOrder).build();
    }

    /**
     * 获取当前用户的订单列表（分页）
     * @param itemStatus
     * @param page
     * @param pageSize
     * @return
     */
    @GET
    @Path("/list")
    public Response getOrderList(
            @QueryParam("itemStatus") String itemStatus,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("page_size") @DefaultValue("5") int pageSize) {

        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());

        // Django 视图从 GET.get('userId') 获取，我们从 JWT 获取
        PaginatedResult<OrderListDto> results = orderService.getOrdersByUserId(currentUserId, itemStatus, page, pageSize);
        return Response.ok(results).build();
    }

    /**
     * 客户更新订单项状态（例如确认收货、申请退款）
     * @param updateDto
     * @return
     */
    @PUT
    @Path("/item/update")
    public Response updateOrderItemStatus(OrderItemStatusUpdateDto updateDto) {
        // 异常 (NotFound, IllegalArgument) 将被捕获
        OrderItem updatedItem = orderService.updateCustomerItemStatus(updateDto);
        // 返回更新后的 DTO（或仅返回状态）
        // 为了与 Django 视图的响应保持一致，我们构造一个简单的 DTO
        var responseDto = new OrderItemStatusUpdateDto(updatedItem.getItemId(), null, updatedItem.getItemStatus());
        return Response.ok(responseDto).build();
    }

    /**
     * 获取单个订单项（用于评论检查）
     * @param itemId
     * @return
     */
    @GET
    @Path("/item/get")
    public Response getOrderItem(@QueryParam("id") String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            throw new WebApplicationException("Query parameter 'id' (item_id) is required.", Response.Status.BAD_REQUEST);
        }
        OrderItemCommentStatusDto itemStatus = orderService.getOrderItemForComment(itemId);
        return Response.ok(itemStatus).build();
    }

    /**
     * 获取未读通知数量
     * @return
     */
    @GET
    @Path("/notification")
    public Response getNotificationCount() {
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());
        OrderNotificationCountDto count = orderService.getNotificationCount(currentUserId);
        return Response.ok(count).build();
    }

    /**
     * 标记所有通知为已读
     * @return
     */
    @PATCH
    @Path("/mark-notification")
    public Response markNotificationsAsRead() {
        UUID currentUserId = UUID.fromString(jwtPrincipal.getName());
        orderService.markNotificationsAsRead(currentUserId);
        return Response.ok().build(); // 返回 200 OK
    }

    /**
     * 管理员：获取所有订单列表（分页）
     * @param itemStatus
     * @param page
     * @param pageSize
     * @return
     */
    @GET
    @Path("/admin/list")
    @RolesAllowed("admin") //
    public Response getAdminOrdersList(
            @QueryParam("itemStatus") String itemStatus,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("page_size") @DefaultValue("10") int pageSize) {

        PaginatedResult<OrderListDto> results = orderService.getAdminOrdersList(itemStatus, page, pageSize);

        // 匹配 Django 的 'get_all_orders_view' 响应格式
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("orders", results.data());
        responseData.put("total", results.totalItems());

        // 返回 {"code": 1, "data": {"orders": [...], "total": ...}}
        return Response.ok(ApiResponseDto.success(responseData)).build();
    }

    /**
     * 管理员：获取订单详情
     * @param orderId
     * @return
     */
    @GET
    @Path("/admin/detail")
    @RolesAllowed("admin") //
    public Response getAdminOrderDetail(@QueryParam("id") UUID orderId) {
        if (orderId == null) {
            throw new WebApplicationException("Query parameter 'id' is required.", Response.Status.BAD_REQUEST);
        }
        OrderDetailResponseDto orderDetails = orderService.getAdminOrderDetail(orderId);
        // 返回 {"code": 1, "data": {...}}
        return Response.ok(ApiResponseDto.success(orderDetails)).build();
    }

    /**
     * 管理员：更新订单项状态（例如：发货、退款完成）
     * @param updateDto
     * @return
     */
    @PUT
    @Path("/admin/item/update")
    @RolesAllowed("admin") //
    public Response updateAdminItemStatus(AdminOrderItemStatusUpdateDto updateDto) {
        if (updateDto.itemId() == null || updateDto.status() == null) {
            throw new WebApplicationException("Request body must contain 'itemId' and 'status'.", Response.Status.BAD_REQUEST);
        }

        orderService.updateAdminItemStatus(updateDto);
        // 对应 Result.success()
        // 返回 {"code": 1}
        return Response.ok(ApiResponseDto.success()).build();
    }
}