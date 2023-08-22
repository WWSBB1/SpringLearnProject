package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author Cheems
 * @Date 2023/8/13 19:41
 * @PackageName:com.sky.mapper
 * @ClassName: OrderDetailMapper
 * @Description: TODO
 * @Version 1.0
 */
@Mapper
public interface OrderDetailMapper {
    void insertBatch(List<OrderDetail> orderDetailList);

    @Select("select * from order_detail where order_id = #{OrderId}")
    List<OrderDetail> getByOrderId(Long OrderId);
}
