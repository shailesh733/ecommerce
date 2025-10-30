package com.example.demo.controller;

import com.example.demo.entity.Item;
import com.example.demo.entity.Order;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.jwt.JwtUtil;
import com.example.demo.repo.ItemRepository;
import com.example.demo.repo.OrderRepository;
import com.example.demo.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final JwtUtil jwtUtil;

    public OrderController(OrderRepository orderRepo, UserRepository userRepo, ItemRepository itemRepo, JwtUtil jwtUtil) {
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Order createOrder(@RequestParam Long userId, @RequestBody List<Long> itemIds) {
        User user = userRepo.findById(userId).orElseThrow();
        List<Item> items = itemRepo.findAllById(itemIds);

        double total = items.stream().mapToDouble(Item::getPrice).sum();

        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotalPrice(total);
        order.setStatus("PLACED");

        return orderRepo.save(order);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepo.findById(id).orElseThrow();
    }

    @DeleteMapping("/{id}")
    public String cancelOrder(@PathVariable Long id) {
        orderRepo.deleteById(id);
        return "Order cancelled successfully!";
    }

    //  Order History API
    @GetMapping("/history")
    public List<Order> getOrderHistory(HttpServletRequest request) {
        // Extract token from Authorization header
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = header.substring(7);
        String email = jwtUtil.extractEmail(token);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepo.findByUser(user);
    }

    // 🆕 Update order status or cancel order
    @PutMapping("/{orderId}")
    public Map<String, Object> updateOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow();

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Only admin can update any order
        // User can only update/cancel their own order
        if (!user.getRole().equals(Role.ADMIN) && !order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to update this order");
        }

        // Update status
        if (updates.containsKey("status")) {
            String newStatus = updates.get("status").toString();

            if (user.getRole().equals(Role.USER) && !newStatus.equalsIgnoreCase("CANCELLED")) {
                throw new RuntimeException("User can only cancel an order");
            }

            // Validation logic: user can't cancel shipped orders
            if (newStatus.equalsIgnoreCase("CANCELLED") && order.getStatus().equalsIgnoreCase("SHIPPED")) {
                throw new RuntimeException("Order already shipped, cannot cancel");
            }

            order.setStatus(newStatus.toUpperCase());
        }

        // Optional: update items
        if (updates.containsKey("itemIds")) {
            @SuppressWarnings("unchecked")
            List<Integer> itemIds = (List<Integer>) updates.get("itemIds");
            List<Long> ids = itemIds.stream().map(Integer::longValue).toList();
            List<Item> items = itemRepo.findAllById(ids);

            order.setItems(items);
            double total = items.stream().mapToDouble(Item::getPrice).sum();
            order.setTotalPrice(total);
        }

        orderRepo.save(order);

        return Map.of(
                "message", "Order updated successfully",
                "orderId", order.getId(),
                "newStatus", order.getStatus()
        );
    }
}

