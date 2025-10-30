package com.example.demo.service;

import com.example.demo.entity.Item;
import com.example.demo.repo.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public List<Item> getAllItems() {
        return repo.findAll();
    }

    public Item getItemById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Item not found!"));
    }

    public Item createItem(Item item) {
        return repo.save(item);
    }

    public Item updateItem(Long id, Item item) {
        Item existing = getItemById(id);
        existing.setName(item.getName());
        existing.setPrice(item.getPrice());
        existing.setCategory(item.getCategory());
        existing.setDescription(item.getDescription());
        return repo.save(existing);
    }

    public void deleteItem(Long id) {
        repo.deleteById(id);
    }
}
