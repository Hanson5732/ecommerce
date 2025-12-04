package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.service.CommentService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/comment") // 对应 /api/comment
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentResource {

    @Inject
    private CommentService commentService;

    /**
     * 获取产品评论列表（分页）
     * @param productId
     * @param page
     * @param pageSize
     * @param ratingFilter
     * @param hasImageOnly
     * @return
     */
    @GET
    @Path("/list")
    public Response getProductComments(
            @QueryParam("productId") String productId,
            @QueryParam("currentPage") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("20") int pageSize,
            @QueryParam("rating") String ratingFilter,
            @QueryParam("hasImage") @DefaultValue("false") boolean hasImageOnly) {

        if (productId == null) {
            throw new WebApplicationException("Query parameter 'productId' is required.", Response.Status.BAD_REQUEST);
        }

        PaginatedResult<CommentResponseDto> results = commentService.getProductComments(
                productId, ratingFilter, hasImageOnly, page, pageSize
        );
        return Response.ok(ApiResponseDto.success(results)).build();
    }

    /**
     * 添加新评论（需要认证）
     * @param commentDto
     * @return
     */
    @POST
    @Path("/add")
    public Response addComment(CommentAddDto commentDto) {
        // 异常 (NotFound) 将被 GlobalExceptionMapper 捕获
        CommentResponseDto newComment = commentService.addComment(commentDto);
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(newComment)).build();
    }

    /**
     * 获取单个评论详情（用于编辑）
     * @param commentId
     * @return
     */
    @GET
    @Path("/get")
    public Response getCommentById(@QueryParam("id") String commentId) {
        if (commentId == null || commentId.isEmpty()) {
            throw new WebApplicationException("Query parameter 'id' (comment_id) is required.", Response.Status.BAD_REQUEST);
        }
        CommentDetailDto comment = commentService.getCommentById(commentId);
        return Response.ok(ApiResponseDto.success(comment)).build();
    }

    /**
     * 更新评论（需要认证）
     * @param updateInputDto
     * @return
     */
    @PUT
    @Path("/update")
    public Response updateComment(CommentUpdateInputDto updateInputDto) {
        // 验证 ID 是否在请求体中
        if (updateInputDto.id() == null || updateInputDto.id().isEmpty()) {
            throw new WebApplicationException("Request body must contain 'id'.", Response.Status.BAD_REQUEST);
        }

        CommentResponseDto updatedComment = commentService.updateComment(updateInputDto);
        return Response.ok(ApiResponseDto.success(updatedComment)).build();
    }

    /**
     * 删除评论（需要认证）
     * @param deleteDto
     * @return
     */
    @DELETE
    @Path("/delete")
    public Response deleteComment(CommentDeleteDto deleteDto) {
        if (deleteDto == null || deleteDto.id() == null || deleteDto.id().isEmpty()) {
            throw new WebApplicationException("Request body with 'id' is required.", Response.Status.BAD_REQUEST);
        }
        commentService.deleteComment(deleteDto.id());
        return Response.ok(ApiResponseDto.success()).build();
    }
}