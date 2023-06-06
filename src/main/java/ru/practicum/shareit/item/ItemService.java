package ru.practicum.shareit.item;

import org.hibernate.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.*;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getItemById(Long userId, Long itemId) throws ObjectNotFoundException;

    List<ItemDto> getAll(Long userId);

    List<ItemDto> search(Long userId, String text);
}
