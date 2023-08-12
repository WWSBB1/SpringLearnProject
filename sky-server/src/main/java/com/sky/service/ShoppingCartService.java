package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @Author Cheems
 * @Date 2023/8/13 5:43
 * @PackageName:com.sky.service
 * @ClassName: ShoppingCartService
 * @Description: TODO
 * @Version 1.0
 */
public interface ShoppingCartService {
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShoppingCart();

    void cleanShoppingCart();

}
