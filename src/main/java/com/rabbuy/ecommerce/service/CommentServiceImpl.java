package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.CommentDao;
import com.rabbuy.ecommerce.dao.OrderItemDao;
import com.rabbuy.ecommerce.dao.ProductDao;
import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.entity.Comment;
import com.rabbuy.ecommerce.entity.OrderItem;
import com.rabbuy.ecommerce.entity.Product;
import com.rabbuy.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@ApplicationScoped
public class CommentServiceImpl implements CommentService {

    @Inject
    private CommentDao commentDao;

    @Inject
    private ProductDao productDao;

    @Inject
    private OrderItemDao orderItemDao;

    // 辅助方法：将 Comment 转换为 CommentResponseDto
    // 注意：这依赖于 CommentDao.findByProductId 预加载了关联数据
    private CommentResponseDto toResponseDto(Comment comment) {
        User user = null;
        if (comment.getOrderItem() != null && comment.getOrderItem().getOrder() != null) {
            user = comment.getOrderItem().getOrder().getUser();
        }

        CommentUserDto userDto = (user != null) ?
                new CommentUserDto(user.getUsername(), user.getProfilePicture()) :
                new CommentUserDto("Unknown User", null); // 兜底

        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getCommentDesc(),
                comment.getRating(),
                comment.getImages(),
                comment.getCreatedTime(), // (在 PrePersist 中设置)
                userDto
        );
    }

    @Override
    public PaginatedResult<CommentResponseDto> getProductComments(String productId, String ratingFilter, boolean hasImageOnly, int page, int pageSize) {
        //
        // 1. DAO 层负责数据库查询和分页
        // ** 假设 CommentDaoImpl.findByProductId 实现了 user 信息的预加载 **
        PaginatedResult<Comment> paginatedComments = commentDao.findByProductId(productId, ratingFilter, hasImageOnly, page, pageSize);

        // 2. Service 层负责将实体转换为 DTO
        List<CommentResponseDto> dtoList = paginatedComments.data().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        // 3. 返回 DTO 分页结果
        return new PaginatedResult<>(
                dtoList,
                paginatedComments.totalItems(),
                paginatedComments.currentPage(),
                paginatedComments.totalPages()
        );
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(CommentAddDto commentDto) throws NotFoundException {
        //
        // 1. 获取关联实体
        OrderItem orderItem = orderItemDao.findById(commentDto.orderItemId())
                .orElseThrow(() -> new NotFoundException("Order item not found"));

        Product product = productDao.findById(commentDto.productId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        // 2. 业务逻辑：更新产品评分
        int newRatingNum = product.getRatingNum() + 1;
        double newRating = ((product.getProductRating() * product.getRatingNum()) + commentDto.rating()) / newRatingNum;

        product.setRatingNum(newRatingNum);
        product.setProductRating(newRating);
        // (事务提交时，JPA 会自动更新受管的 product 实体)

        // 3. 业务逻辑：更新订单项状态
        orderItem.setItemStatus("8"); // "8" = Done
        // (事务提交时，JPA 会自动更新受管的 orderItem 实体)

        // 4. 创建新评论
        Comment comment = new Comment();
        comment.setCommentId(commentDto.orderItemId()); // 使用 OrderItem ID 作为 Comment ID
        comment.setRating(commentDto.rating());
        comment.setCommentDesc(commentDto.commentDesc() != null ? commentDto.commentDesc() : "The user didn't say anything");
        comment.setImages(commentDto.images() != null ? commentDto.images() : new ArrayList<>());
        comment.setOrderItem(orderItem);
        comment.setProduct(product);
        // (created_time 和 updated_time 由 @PrePersist 设置)

        commentDao.save(comment);

        // 5. 返回 DTO
        return toResponseDto(comment);
    }

    @Override
    public CommentDetailDto getCommentById(String commentId) throws NotFoundException {
        //
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        ProductSnapshot snapshot = comment.getOrderItem().getProduct();

        CommentDetailDto.OrderItemSnapshotDto itemDto = new CommentDetailDto.OrderItemSnapshotDto(
                snapshot.getImage(),
                snapshot.getName()
        );

        return new CommentDetailDto(
                comment.getCommentId(),
                comment.getCommentDesc(),
                comment.getImages(),
                comment.getUpdatedTime(),
                comment.getRating(),
                itemDto
        );
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(CommentUpdateInputDto updateInputDto) throws NotFoundException {
        // 从 DTO 中提取 ID
        String commentId = updateInputDto.id();
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID is required in update DTO");
        }

        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        Product product = comment.getProduct();
        double oldRating = comment.getRating();
        double newRating = updateInputDto.rating();

        // 1. 业务逻辑：重新计算产品评分
        if (product.getRatingNum() > 0) {
            double totalRating = (product.getProductRating() * product.getRatingNum()) - oldRating + newRating;
            product.setProductRating(totalRating / product.getRatingNum());
            // (事务提交时自动更新)
        }

        // 2. 更新评论
        comment.setRating(newRating);
        String newDesc = updateInputDto.commentDesc() != null ? updateInputDto.commentDesc() : "The user didn't say anything";
        comment.setCommentDesc(newDesc);
        comment.setImages(updateInputDto.images() != null ? updateInputDto.images() : new ArrayList<>());
        // (updated_time 由 @PreUpdate 自动设置)
        Comment updatedComment = commentDao.update(comment);

        // 3. 返回 DTO
        return toResponseDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(String commentId) throws NotFoundException {
        //
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        Product product = comment.getProduct();
        double oldRating = comment.getRating();

        // 1. 业务逻辑：重新计算产品评分
        if (product.getRatingNum() > 1) {
            int newRatingNum = product.getRatingNum() - 1;
            double newRating = ((product.getProductRating() * product.getRatingNum()) - oldRating) / newRatingNum;
            product.setRatingNum(newRatingNum);
            product.setProductRating(newRating);
        } else {
            // 这是最后一条评论
            product.setRatingNum(0);
            product.setProductRating(0.0);
        }
        // (事务提交时自动更新)

        // 2. 删除评论
        commentDao.delete(comment);
    }
}