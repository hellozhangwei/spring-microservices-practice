package com.github.zhangwei1979.orderservice;

import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {
    private final List<Order> orders = Arrays.asList(
            new Order(1, 1, "Product A"),
            new Order(2, 1, "Product B"),
            new Order(3, 2, "Product C"),
            new Order(4, 1, "Product D"),
            new Order(5, 2, "Product E"));

    @GetMapping
    public List<Order> getAllOrders(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) {
            return orders.stream()
                    .filter(order -> customerId.equals(order.getCustomerId()))
                    .collect(Collectors.toList());
        }

        return orders;
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable int id) {
        return orders.stream()
                     .filter(order -> order.getId() == id)
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}