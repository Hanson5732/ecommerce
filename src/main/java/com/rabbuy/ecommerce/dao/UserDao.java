package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.User;
import java.util.List;
import java.util.Optional;

// User 数据访问对象接口
public interface UserDao {

    void save(User user); // 保存（创建或更新）

    Optional<User> findById(String id); // 根据 ID 查找

    Optional<User> findByUsername(String username); // 根据用户名查找

    Optional<User> findByEmail(String email); // 根据 Email 查找

    List<User> findAll(); // 查找所有用户 (谨慎使用，可能数据量很大)

    void update(User user); // 更新

    void delete(User user); // 删除

    void deleteById(String id); // 根据 ID 删除

    boolean existsByUsername(String username); // 检查用户名是否存在

    boolean existsByEmail(String email); // 检查 Email 是否存在
}