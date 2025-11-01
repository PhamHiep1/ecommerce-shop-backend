package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(()-> new DataNotFoundException(
                     "cannot found user with id"+orderDTO.getUserId()));

        // Use Model Mapper libray
        // Create a separate mapping thread to control the mapping
        modelMapper.typeMap(OrderDTO.class,Order.class)
                .addMappings(
                        mapper
                                -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderDTO,order);
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        LocalDate shippingDate = orderDTO.getShippingDate() == null
                ? LocalDate.now() : orderDTO.getShippingDate() ;

        // check shippingDate is greater than current time
        if(shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("shipping date must be least today");
        }

        order.setActive(true);
        orderRepository.save(order);

        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public List<Order> findOrderByUserId(Long userId)
    {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order updateOrderById(Long id, OrderDTO orderDTO)
            throws DataNotFoundException {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(()->new DataNotFoundException(
                        "cannot find order with id = "+id));
        User existingUser = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find user with id "+ orderDTO.getUserId()
                ));

        modelMapper.typeMap(OrderDTO.class,Order.class)
                .addMappings(
                        mapper
                                -> mapper.skip(Order::setId));

        modelMapper.map(orderDTO,existingOrder);
        existingOrder.setUser(existingUser);
        orderRepository.save(existingOrder);

        return existingOrder;
    }

    @Override
    public void deleteOrderById(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        // soft-delete
        if(order != null){
            order.setActive(false);
            orderRepository.save(order);
        }
    }
}
