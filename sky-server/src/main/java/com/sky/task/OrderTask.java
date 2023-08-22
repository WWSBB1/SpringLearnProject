package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Cheems
 * @Date 2023/8/22 11:18
 * @PackageName:com.sky.task
 * @ClassName: OrderTask
 * @Description:
 * @Version 1.0
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeoutOrder(){
        log.info("定时处理超时订单: {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders order : ordersList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时,自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单: {}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}