package com.sky.service;

import com.sky.dto.*;
import com.sky.vo.*;

/**
 * @Author Cheems
 * @Date 2023/8/13 19:25
 * @PackageName:com.sky.service
 * @ClassName: OrderService
 * @Description: TODO
 * @Version 1.0
 */
public interface OrderService {
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);
}
