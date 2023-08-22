package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author Cheems
 * @Date 2023/8/13 19:25
 * @PackageName:com.sky.service.impl
 * @ClassName: OrderServiceImpl
 * @Version 1.0
 */

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //异常情况的处理（收货地址为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        //查询当前用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());

        //向订单表插入1条数据
        orderMapper.insert(order);

        //订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }

        //向明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        //清理购物车中的数据
        shoppingCartMapper.deleteByUserId(userId);

        //封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders order = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(order);

        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",order.getId());
        map.put("content","订单号: "+ outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders =orderMapper.getById(id);
        List<OrderDetail> orderDetailList =orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void userCancelById(Long id) throws Exception {
//        orderMapper.deleteById(id);
        //先检查是否存在
        Orders order = orderMapper.getById(id);
        if (order==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (order.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    order.getAmount(),
                    order.getAmount()
            );
            order.setPayStatus(Orders.REFUND);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason("用户取消");
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);
    }

    @Override
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(ordersConfirmDTO.getStatus())
                .build();
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders order = orderMapper.getById(ordersRejectionDTO.getId());
        if (order==null||!order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (order.getPayStatus()== Orders.PAID){
            String refund = weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    order.getAmount(),
                    order.getAmount()
                    );
            log.info("申请退款：{}", refund);
        }
        order.setId(order.getId());
        order.setStatus(Orders.CANCELLED);
        order.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());

        orderMapper.update(order);

    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //获取对象
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        //检查
        if(order.getPayStatus()==1){
            String refund = weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    order.getAmount(),
                    order.getAmount()
            );
            log.info("申请退款：{}", refund);
        }
        //处理
        order.setId(ordersCancelDTO.getId());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        order.setCancelTime(LocalDateTime.now());
        //更新
        orderMapper.update(order);
    }

    @Override
    public void delivery(Long id) {
        //获取对象
        Orders order = orderMapper.getById(id);
        //检查
        if(order==null||!order.getStatus().equals(Orders.CONFIRMED)){

        }
        //处理

        order.setId(order.getId());
            // 更新订单状态,状态转为派送中
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);

        //更新
        orderMapper.update(order);
    }

    @Override
    public void complete(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null || !order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setId(order.getId());
        // 更新订单状态,状态转为完成
        order.setStatus(Orders.COMPLETED);
        order.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(order);
    }

    @Override
    public void reminder(Long id) {
        Orders order = orderMapper.getById(id);
        if(order!=null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap();
        map.put("type",2);//1表示来单提醒2表示客户催单
        map.put("orderId",id);
        map.put("content","订单号："+order.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }


}
