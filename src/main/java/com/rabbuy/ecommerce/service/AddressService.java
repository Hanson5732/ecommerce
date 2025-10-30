package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.AddressDto;
import com.rabbuy.ecommerce.dto.AddressInputDto;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    /**
     * 获取指定用户的所有地址
     *
     */
    List<AddressDto> getAddressesByUserId(UUID userId);

    /**
     * 为指定用户添加新地址
     *
     * @throws IllegalStateException 如果地址超过 5 个
     * @throws NotFoundException 如果用户不存在
     */
    AddressDto addAddress(UUID userId, AddressInputDto addressDto) throws IllegalStateException, NotFoundException;

    /**
     * 更新指定 ID 的地址
     *
     * @throws NotFoundException 如果地址不存在
     */
    AddressDto updateAddress(UUID addressId, AddressInputDto addressDto) throws NotFoundException;

    /**
     * 删除指定 ID 的地址
     *
     * @throws NotFoundException 如果地址不存在
     */
    void deleteAddress(UUID addressId) throws NotFoundException;
}