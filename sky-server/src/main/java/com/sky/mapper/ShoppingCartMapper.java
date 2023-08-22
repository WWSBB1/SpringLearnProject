package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author Cheems
 * @Date 2023/8/13 5:52
 * @PackageName:com.sky.mapper
 * @ClassName: ShoppingCartMapper
 * @Description: TODO
 * @Version 1.0
 */
@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number=#{number} where id=#{id} ")
    void updateNumberById(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) VALUES " +
            "(#{name},#{image},#{user_id},#{dish_id},#{setmeal_id},#{dish_flavor},#{amount},#{create_time})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
