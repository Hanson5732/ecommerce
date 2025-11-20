package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.AddressDto;
import com.rabbuy.ecommerce.dto.AddressInputDto;
import com.rabbuy.ecommerce.service.AddressService;
import com.rabbuy.ecommerce.dto.ApiResponseDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

@Path("/address") // 对应 /api/address
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AddressResource {

    @Inject
    private AddressService addressService;

    @Context
    private SecurityContext securityContext;

    /**
     * 获取当前用户的所有地址
     * 对应: path('', views.get_address_view, ...)
     * 访问: GET /api/address
     */
    @GET
    public Response getAddresses() {
        // 从 JWT 获取用户 ID，而不是从查询参数
        String currentUserId = securityContext.getUserPrincipal().getName();
        List<AddressDto> addresses = addressService.getAddressesByUserId(currentUserId);
        return Response.ok(ApiResponseDto.success(addresses)).build();
    }

    /**
     * 为当前用户添加新地址
     * 对应: path('add/', views.add_address_view, ...)
     * 访问: POST /api/address/add
     */
    @POST
    @Path("/add")
    public Response addAddress(AddressInputDto addressDto) {
        String currentUserId = securityContext.getUserPrincipal().getName();
        // 业务异常 (IllegalStateException, NotFoundException) 将被 GlobalExceptionMapper 捕获
        AddressDto newAddress = addressService.addAddress(currentUserId, addressDto);
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(newAddress)).build();
    }

    /**
     * 更新当前用户的特定地址
     * 对应: (Django view: update_address_view)
     * 访问: PUT /api/address/{id}
     */
    @PUT
    @Path("/{id}")
    public Response updateAddress(@PathParam("id") String addressId, AddressInputDto addressDto) {
        String currentUserId = securityContext.getUserPrincipal().getName();
        // 业务异常 (NotFoundException, SecurityException) 将被 Service 层和 Mapper 捕获
        AddressDto updatedAddress = addressService.updateAddress(addressId, currentUserId, addressDto);
        return Response.ok(ApiResponseDto.success(updatedAddress)).build();
    }

    /**
     * 删除当前用户的特定地址
     * 对应: (Django view: delete_address_view)
     * 访问: DELETE /api/address/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteAddress(@PathParam("id") String addressId) {
        String currentUserId = securityContext.getUserPrincipal().getName();
        // 业务异常 (NotFoundException, SecurityException) 将被 Service 层和 Mapper 捕获
        addressService.deleteAddress(addressId, currentUserId);
        return Response.ok(ApiResponseDto.success("Address deleted successfully")).build();
    }
}