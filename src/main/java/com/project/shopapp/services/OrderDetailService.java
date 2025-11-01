package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailService implements IOrderDetailService{
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        Product existingProduct = productRepository
                .findById(orderDetailDTO.getProductId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find product with id = "+ orderDetailDTO.getProductId()
                ));
        Order existingOrder = orderRepository
                .findById(orderDetailDTO.getOrderId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find order with id = "+ orderDetailDTO.getOrderId()
                ));

        OrderDetail orderDetail = OrderDetail.builder()
                .order(existingOrder)
                .product(existingProduct)
                .color(orderDetailDTO.getColor())
                .price(orderDetailDTO.getPrice())
                .numberOfProduct(orderDetailDTO.getNumberOfProduct())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .build();
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetailById(Long id) throws DataNotFoundException {
        return orderDetailRepository
                .findById(id)
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find order detail with id + " + id));
    }

    @Override
    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailDTO) throws DataNotFoundException {
        Product existingProduct = productRepository
                .findById(orderDetailDTO.getProductId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find product with id = "+ orderDetailDTO.getProductId()
                ));
        Order existingOrder = orderRepository
                .findById(orderDetailDTO.getOrderId())
                .orElseThrow(()-> new DataNotFoundException(
                        "cannot find order with id = "+ orderDetailDTO.getOrderId()
                ));

        OrderDetail existingOrderDetail = getOrderDetailById(id);

         existingOrderDetail = OrderDetail.builder()
                 .id(id)
                .order(existingOrder)
                .product(existingProduct)
                .color(orderDetailDTO.getColor())
                .price(orderDetailDTO.getPrice())
                .numberOfProduct(orderDetailDTO.getNumberOfProduct())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .build();
        return orderDetailRepository.save(existingOrderDetail);

    }

    @Override
    public void deleteOrderDetail(Long id) {
        orderDetailRepository.deleteById(id);
    }
}
