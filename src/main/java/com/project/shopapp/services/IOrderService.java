package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    Order createOrder(OrderDTO orderDTO) throws Exception;
    Order getOrderById(Long id);
    List<Order> findOrderByUserId(Long userId);
    Order updateOrderById(Long id, OrderDTO orderDTO) throws DataNotFoundException;
    void deleteOrderById(Long id);

}
