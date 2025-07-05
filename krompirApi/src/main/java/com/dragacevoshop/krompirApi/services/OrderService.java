package com.dragacevoshop.krompirApi.services;

import com.dragacevoshop.krompirApi.models.Order;
import com.dragacevoshop.krompirApi.models.OrderItem;
import com.dragacevoshop.krompirApi.models.Product;
import com.dragacevoshop.krompirApi.repositories.OrderRepository;
import com.dragacevoshop.krompirApi.repositories.ProductRepository;
import com.dragacevoshop.krompirApi.requests.OrderItemRequest;
import com.dragacevoshop.krompirApi.requests.OrderRequest;
import com.dragacevoshop.krompirApi.utils.OrderFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    @Autowired
    private EmailService emailService;

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setAddress(request.getAddress());
        order.setComment(request.getComment());
        order.setCity(request.getCity());
        order.setPhone(request.getPhone());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

            OrderItem item = new OrderItem();
            item.setOrder(order); // bidirectional link
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());

            orderItems.add(item);
        }

        order.setItems(orderItems);
        
        String formattedOrder = OrderFormatter.formatOrder(order);
        emailService.sendOrderSummary("markotomasevic624@gmail.com", "New Order Received", formattedOrder);
        emailService.sendOrderSummary(order.getCustomerEmail(), "New Order Received", "Napavili ste porudzbinu:\n\n" + formattedOrder);

        return orderRepository.save(order);
    }
}
