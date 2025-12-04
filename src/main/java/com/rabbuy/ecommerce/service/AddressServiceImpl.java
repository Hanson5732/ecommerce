package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.AddressDao;
import com.rabbuy.ecommerce.dao.UserDao;
import com.rabbuy.ecommerce.dto.AddressDto;
import com.rabbuy.ecommerce.dto.AddressInputDto;
import com.rabbuy.ecommerce.entity.Address;
import com.rabbuy.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped // 声明为 CDI Bean
public class AddressServiceImpl implements AddressService {

    @Inject
    private AddressDao addressDao;

    @Inject // 注入 User DAO (添加地址时需要)
    private UserDao userDao;

    private static final int MAX_ADDRESSES_PER_USER = 5;

    // 辅助方法：将 Address 实体转换为 AddressDto
    private AddressDto toDto(Address entity) {
        return new AddressDto(
                entity.getAddressId(),
                entity.getAddressTag(),
                entity.getRecipientName(),
                entity.getPhone(),
                entity.getProvince(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getAdditionalAddress(),
                entity.getPostalCode(),
                entity.isDefault()
        );
    }

    @Override
    public List<AddressDto> getAddressesByUserId(String userId) {
        //
        return addressDao.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // 涉及数据库写入，需要事务
    public AddressDto addAddress(String userId, AddressInputDto addressDto) throws IllegalStateException, NotFoundException {
        //
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 业务规则 1：检查地址数量限制
        long addressCount = addressDao.countByUserId(userId);
        if (addressCount >= MAX_ADDRESSES_PER_USER) {
            throw new IllegalStateException("Each user can create up to 5 addresses");
        }

        // 业务规则 2：如果这是第一个地址，则设为默认
        boolean isDefault = (addressCount == 0);

        // 如果用户在 DTO 中明确指定了 isDefault (即使是第一个地址)，则覆盖
        if (addressDto.isDefault() != null) {
            isDefault = addressDto.isDefault();
        }

        // 如果要将这个新地址设为默认，则取消其他默认地址
        if (isDefault) {
            unsetOtherDefaults(userId, null); // null 表示没有要排除的 ID
        }

        Address newAddress = new Address();
        newAddress.setUser(user);
        newAddress.setAddressTag(addressDto.tag());
        newAddress.setRecipientName(addressDto.recipient());
        newAddress.setPhone(addressDto.phone());
        newAddress.setProvince(addressDto.province());
        newAddress.setCity(addressDto.city());
        newAddress.setDistrict(addressDto.district());
        newAddress.setAdditionalAddress(addressDto.additionalAddress());
        newAddress.setPostalCode(addressDto.postalCode() != null ? addressDto.postalCode() : "000000"); // 默认邮编
        newAddress.setDefault(isDefault);

        addressDao.save(newAddress);

        return toDto(newAddress);
    }

    @Override
    @Transactional
    public AddressDto updateAddress(String addressId, String currentUserId, AddressInputDto addressDto) throws NotFoundException, SecurityException {
        //
        Address address = addressDao.findById(addressId) //
                .orElseThrow(() -> new NotFoundException("Address not found"));

        // *** 安全检查：验证地址是否属于当前用户 ***
        if (!address.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("User not authorized to update this address");
        }

        // 业务规则：处理默认地址更新
        if (addressDto.isDefault() != null && addressDto.isDefault() && !address.isDefault()) {
            unsetOtherDefaults(currentUserId, addressId);
            address.setDefault(true);
        } else if (addressDto.isDefault() != null && !addressDto.isDefault()) {
            address.setDefault(false);
        }

        // 更新其他字段
        if (addressDto.tag() != null) address.setAddressTag(addressDto.tag());
        if (addressDto.recipient() != null) address.setRecipientName(addressDto.recipient());
        if (addressDto.phone() != null) address.setPhone(addressDto.phone());
        if (addressDto.province() != null) address.setProvince(addressDto.province());
        if (addressDto.city() != null) address.setCity(addressDto.city());
        if (addressDto.district() != null) address.setDistrict(addressDto.district());
        if (addressDto.additionalAddress() != null) address.setAdditionalAddress(addressDto.additionalAddress());
        if (addressDto.postalCode() != null) address.setPostalCode(addressDto.postalCode());

        Address updatedAddress = addressDao.update(address);
        return toDto(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String addressId, String currentUserId) throws NotFoundException, SecurityException {
        //
        Address address = addressDao.findById(addressId) //
                .orElseThrow(() -> new NotFoundException("Address not found"));

        // *** 安全检查：验证地址是否属于当前用户 ***
        if (!address.getUser().getId().equals(currentUserId)) {
            throw new SecurityException("User not authorized to delete this address");
        }

        boolean wasDefault = address.isDefault();

        // 删除地址
        addressDao.delete(address); //

        // 业务规则：如果删除的是默认地址，则需要设置一个新的默认地址
        if (wasDefault) {
            List<Address> remainingAddresses = addressDao.findByUserId(currentUserId); //
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                addressDao.update(newDefault); //
            }
        }
    }

    /**
     * 辅助方法：将用户的所有其他地址设置为非默认
     * @param userId 用户ID
     * @param excludeAddressId (可选) 要排除的地址ID（例如正在被设为默认的地址）
     */
    @Transactional
    private void unsetOtherDefaults(String userId, String excludeAddressId) {
        // 查找该用户的所有非默认地址（此方法在 AddressDao 中）
        List<Address> defaults = addressDao.findNonDefaultsByUserId(userId);

        for (Address addr : defaults) {
            if (addr.isDefault() && (excludeAddressId == null || !addr.getAddressId().equals(excludeAddressId))) {
                addr.setDefault(false);
                addressDao.update(addr); //
            }
        }
    }
}