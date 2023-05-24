package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemDaoImpl implements ItemDao {

    private final Map<Long, List<Item>> items = new HashMap<>();

    Long itemId = 1L;

    public Long generatedId() {
        return itemId++;
    }


    @Override
    public Item create(Item item) {
        item.setId(generatedId());
        List<Item> list = new ArrayList<>();
        list.add(item);
        items.put(item.getOwner().getId(), list);
        return item;
    }

    @Override
    public Item update(Item item) {
        List<Item> userItems = items.get(item.getOwner().getId());
        List<Item> toRemove = new ArrayList<>();
        for (Item userItem : userItems) {
            if (userItem.getId().equals(item.getId())) {
                toRemove.add(userItem);
            }
        }
        userItems.removeAll(toRemove);
        userItems.add(item);

        return item;
    }


    @Override
    public Optional<Item> getItemById(Long itemId) {
        for (List<Item> itemList : items.values()) {
            for (Item item : itemList) {
                if (item.getId().equals(itemId)) {
                    return Optional.of(item);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Item> getAll(Long userId) {
        return items.get(userId);
    }

    @Override
    public List<Item> search(String text) {
        List<Item> itemListSearch = new ArrayList<>();
        for (List<Item> itemList : items.values()) {
            for (Item item : itemList) {
                if (item.getAvailable().equals(true)) {
                    if (item.getName().toLowerCase().contains(text.toLowerCase()) || item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                        itemListSearch.add(item);
                    }
                }
            }
        }
        return itemListSearch;
    }
}
