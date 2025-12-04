package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.dto.PaginatedResult;
import com.rabbuy.ecommerce.entity.Comment;
import java.util.Optional;
import java.util.UUID;

// Comment 数据访问对象接口
public interface CommentDao {

    /**
     * 保存新评论
     */
    void save(Comment comment);

    /**
     * 根据主键 ID 查找评论
     */
    Optional<Comment> findById(String commentId); // 主键是 String (item_id)

    /**
     * 更新评论
     */
    Comment update(Comment comment);

    /**
     * 删除评论
     */
    void delete(Comment comment);

    /**
     * 检查评论是否存在（根据 OrderItem ID）
     */
    boolean existsByOrderItemId(String orderItemId);

    /**
     * 分页查找产品的所有评论
     * @param productId 产品ID
     * @param ratingFilter 评分过滤 (例如 "5", "3", "1", null)
     * @param hasImageOnly 是否只显示有图片的
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PaginatedResult<Comment> findByProductId(String productId, String ratingFilter, boolean hasImageOnly, int page, int pageSize);
}