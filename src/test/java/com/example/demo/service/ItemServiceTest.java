package com.example.demo.service;

import com.example.demo.entity.Item;
import com.example.demo.repo.ItemRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = new Item(1L, "Laptop","Electronics", 59999.0,"it is electronic device");
    }

    @Test
    void testCreateItem() {
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        Item savedItem = itemService.createItem(item);

        assertNotNull(savedItem);
        assertEquals("Laptop", savedItem.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testGetAllItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item));

        List<Item> items = itemService.getAllItems();

        assertEquals(1, items.size());
        assertEquals("Laptop", items.get(0).getName());
    }
}
