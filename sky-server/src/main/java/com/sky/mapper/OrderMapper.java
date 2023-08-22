package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.DishVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Cheems
 * @Date 2023/8/13 19:38
 * @PackageName:com.sky.mapper
 * @ClassName: OrderMapper
 * @Description: TODO
 * @Version 1.0
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);
}
