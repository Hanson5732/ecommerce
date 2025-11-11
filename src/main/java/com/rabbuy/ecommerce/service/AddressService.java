package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.AddressDto;
import com.rabbuy.ecommerce.dto.AddressInputDto;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

public interface AddressService {

    /**
     * 获取指定用户的所有地址
     *
     */
    List<AddressDto> getAddressesByUserId(String userId);

    /**
     * 为指定用户添加新地址
     *
     * @throws IllegalStateException 如果地址超过 5 个
     * @throws NotFoundException 如果用户不存在
     */
    AddressDto addAddress(String userId, AddressInputDto addressDto) throws IllegalStateException, NotFoundException;

    /**
     * 更新指定 ID 的地址
     * @param addressId 要更新的地址 ID
     * @param currentUserId 执行此操作的已登录用户的 ID
     * @param addressDto 更新的数据
     * @throws NotFoundException 如果地址不存在
     * @throws SecurityException 如果用户无权修改此地址
     */
    AddressDto updateAddress(String addressId, String currentUserId, AddressInputDto addressDto) throws NotFoundException, SecurityException;

    /**
     * 删除指定 ID 的地址
     * @param addressId 要删除的地址 ID
     * @param currentUserId 执行此操作的已登录用户的 ID
     * @throws NotFoundException 如果地址不存在
     * @throws SecurityException 如果用户无权删除此地址
     */
    void deleteAddress(String addressId, String currentUserId) throws NotFoundException, SecurityException;
}