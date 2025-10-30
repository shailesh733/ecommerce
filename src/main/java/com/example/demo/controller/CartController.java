package com.example.demo.controller;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;
import com.example.demo.repo.CartRepository;
import com.example.demo.repo.ItemRepository;
import com.example.demo.repo.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartRepository cartRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    public CartController(CartRepository cartRepo, UserRepository userRepo, ItemRepository itemRepo) {
        this.cartRepo = cartRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
    }

    @PostMapping("/add")
    public Cart addToCart(@RequestParam Long userId, @RequestParam Long itemId) {
        User user = userRepo.findById(userId).orElseThrow();
        Item item = itemRepo.findById(itemId).orElseThrow();

        Cart cart = cartRepo.findByUser(user).orElse(new Cart(null, user, new ArrayList<>()));
        cart.getItems().add(item);
        return cartRepo.save(cart);
    }

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return cartRepo.findByUser(user).orElseThrow();
    }

    @DeleteMapping("/remove")
    public Cart removeFromCart(@RequestParam Long userId, @RequestParam Long itemId) {
        User user = userRepo.findById(userId).orElseThrow();
        Cart cart = cartRepo.findByUser(user).orElseThrow();
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return cartRepo.save(cart);
    }
}

