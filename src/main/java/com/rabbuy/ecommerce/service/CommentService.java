package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.CommentAddDto;
import com.rabbuy.ecommerce.dto.CommentDetailDto;
import com.rabbuy.ecommerce.dto.CommentResponseDto;
import com.rabbuy.ecommerce.dto.CommentUpdateInputDto;
import com.rabbuy.ecommerce.dto.PaginatedResult;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

public interface CommentService {

    /**
     * 获取产品评论（分页）
     *
     */
    PaginatedResult<CommentResponseDto> getProductComments(String productId, String ratingFilter, boolean hasImageOnly, int page, int pageSize);

    /**
     * 添加评论（包含更新产品评分和订单项状态的业务逻辑）
     *
     */
    CommentResponseDto addComment(CommentAddDto commentDto) throws NotFoundException;

    /**
     * 获取单个评论详情（用于编辑）
     *
     */
    CommentDetailDto getCommentById(String commentId) throws NotFoundException;

    /**
     * 更新评论（包含更新产品评分的业务逻辑）
     *
     */
    @Transactional
    CommentResponseDto updateComment(CommentUpdateInputDto commentDto) throws NotFoundException;

    /**
     * 删除评论（包含更新产品评分的业务逻辑）
     *
     */
    void deleteComment(String commentId) throws NotFoundException;
}