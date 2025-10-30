package com.example.demo.controller;

import com.example.demo.entity.Item;
import com.example.demo.service.ItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("api/items")
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<Item> getAll() {
        return service.getAllItems();
    }

    @GetMapping("/{id}")
    public Item getById(@PathVariable Long id) {
        return service.getItemById(id);
    }

    @PostMapping
    public Item create(@RequestBody Item item) {
        return service.createItem(item);
    }

    @PutMapping("/{id}")
    public Item update(@PathVariable Long id, @RequestBody Item item) {
        return service.updateItem(id, item);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteItem(id);
        return "Item deleted successfully!";
    }
}
