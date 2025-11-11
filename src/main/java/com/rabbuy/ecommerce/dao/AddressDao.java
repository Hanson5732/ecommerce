package com.rabbuy.ecommerce.dao;

import com.rabbuy.ecommerce.entity.Address;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Address 数据访问对象接口
public interface AddressDao {


    // 根据主键 ID 查找地址
    Optional<Address> findById(String id);

    // 查找特定用户的所有地址
    List<Address> findByUserId(String userId);

    // 查找特定用户的默认地址
    Optional<Address> findDefaultByUserId(UUID userId);

    // 查找特定用户的所有非默认地址
    List<Address> findNonDefaultsByUserId(String userId);

    // 统计特定用户的地址数量
    long countByUserId(String userId);

    // 保存新地址
    void save(Address address);

    // 更新现有地址
    Address update(Address address);

    // 删除地址
    void delete(Address address);
}