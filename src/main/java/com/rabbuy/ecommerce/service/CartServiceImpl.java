package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.CartDao;
import com.rabbuy.ecommerce.dao.ProductDao;
import com.rabbuy.ecommerce.dto.CartDetailItemDto;
import com.rabbuy.ecommerce.dto.CartItem;
import com.rabbuy.ecommerce.dto.CartResponseDto;
import com.rabbuy.ecommerce.entity.Cart;
import com.rabbuy.ecommerce.entity.Product;
import com.rabbuy.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped // CDI Bean
public class CartServiceImpl implements CartService {

    @Inject // 注入 Cart DAO
    private CartDao cartDao;

    @Inject // 注入 Product DAO (用于获取实时商品详情)
    private ProductDao productDao;

    @Override
    public CartResponseDto getCartByUserId(String userId) throws NotFoundException {
        // 1. 获取购物车实体
        Cart cart = cartDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        List<CartItem> storedItems = cart.getProducts();
        List<CartDetailItemDto> detailedItems = new ArrayList<>();

        // 2. 遍历购物车中存储的商品，查询实时数据
        for (CartItem item : storedItems) {
            String productId = item.getId();
            int count = item.getCount();

            // 3. 查询实时商品信息 (使用 findActiveById 检查商品是否有效)
            // Django 直接 Product.objects.get，我们用 findById (包含所有状态)
            Optional<Product> productOpt = productDao.findById(productId);

            if (productOpt.isPresent()) {
                Product p = productOpt.get();

                // 4. 检查商品状态 (Django 中的 status 逻辑)
                boolean isValid = !p.isDeleted() && "1".equals(p.getStatus()) && p.getStockQuantity() > 0;

                detailedItems.add(new CartDetailItemDto(
                        p.getProductId(),
                        p.getProductName(),
                        count,
                        p.getPrice(), // 实时价格
                        (p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().get(0) : null, // 实时图片
                        isValid,  // 实时状态
                        isValid   // 实时是否可选
                ));
            } else {
                // 如果商品在数据库中被物理删除了（但在购物车列表里还有引用）
                detailedItems.add(new CartDetailItemDto(
                        productId,
                        "Product not found", // 或 "商品已失效"
                        count,
                        BigDecimal.ZERO,
                        null,
                        false, // 无效状态
                        false  // 不可选
                ));
            }
        }

        // 5. 构建并返回 DTO
        return new CartResponseDto(
                cart.getCartId(),
                cart.getUser().getId(),
                detailedItems,
                cart.getCreatedTime(),
                cart.getUpdatedTime()
        );
    }

    @Override
    @Transactional // 写入操作，需要事务
    public void saveCart(String userId, List<CartItem> itemsDto) throws NotFoundException {
        //
        Cart cart = cartDao.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        // 业务逻辑：完全覆盖
        cart.setProducts(itemsDto); // itemsDto 是 List<CartItem>，AttributeConverter 会自动处理

        cartDao.update(cart); // 持久化更改
    }

    @Override
    @Transactional
    public void createEmptyCart(User user) {
        //
        Cart newCart = new Cart();
        newCart.setUser(user);
        cartDao.save(newCart);
    }
}