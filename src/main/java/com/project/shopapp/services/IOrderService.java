package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getAllOrder();
    OrderResponse updateOrderById(Long id, OrderDTO orderDTO);
    void deleteOrderById(Long id);

}
