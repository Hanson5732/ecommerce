package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 接口定义了 Category 数据访问对象应有的操作
public interface CategoryDao {

    void save(Category category); // 保存（创建或更新）

    Optional<Category> findById(UUID id); // 根据 ID 查找

    List<Category> findAll(); // 查找所有

    List<Category> findActiveCategories(int limit); // 查找指定数量的活动分类

    void update(Category category); // 更新

    void delete(Category category); // 删除

    void deleteById(UUID id); // 根据 ID 删除
}