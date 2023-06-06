package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemDao itemDao;
    private final UserService userService;

    public ItemDto create(Long userId, ItemDto itemDto) {
        UserDto user = userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(UserMapper.toUser(user));
        return ItemMapper.toItemDto(itemDao.create(item));
    }

    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        UserDto user = userService.getUserById(userId);
        Optional<Item> itemOptional = itemDao.getItemById(itemId);
        if (itemOptional.isPresent()) {
            if (!itemOptional.get().getOwner().getId().equals(userId)) {
                log.debug("Пользователь с id {} не является владельцем вещи под id {}", userId, itemId);
                throw new NotFoundException(String.format("Пользователь с id %s " +
                        "не является владельцем вещи id %s.", userId, itemId));
            }
            Item itemFromStorage = itemOptional.get();
            Item item = ItemMapper.toItem(itemDto);
            if (Objects.isNull(item.getAvailable())) {
                item.setAvailable(itemFromStorage.getAvailable());
            }
            if (Objects.isNull(item.getDescription())) {
                item.setDescription(itemFromStorage.getDescription());
            }
            if (Objects.isNull(item.getName())) {
                item.setName(itemFromStorage.getName());
            }
            item.setId(itemFromStorage.getId());
            item.setRequest(itemFromStorage.getRequest());
            item.setOwner(itemFromStorage.getOwner());

            return ItemMapper.toItemDto(itemDao.update(item));
        }
        return null;
    }

    public ItemDto getItemById(Long userId, Long itemId) throws ObjectNotFoundException {
        userService.getUserById(userId);
        Optional<Item> itemGet = itemDao.getItemById(itemId);
        if (itemGet.isEmpty()) {
            log.debug("У пользователя с id {} не существует вещи с id {}", userId, itemId);
            throw new ru.practicum.shareit.exceptions.NotFoundException(String.format("У пользователя с id %s не " +
                    "существует вещи с id %s", userId, itemId));
        }
        return ItemMapper.toItemDto(itemGet.get());
    }

    public List<ItemDto> getAll(Long userId) {
        userService.getUserById(userId);
        List<Item> itemList = itemDao.getAll(userId);
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemDtoList.add(ItemMapper.toItemDto(item));
        }
        return itemDtoList;
    }

    public List<ItemDto> search(Long userId, String text) {
        userService.getUserById(userId);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> itemList = itemDao.search(text);
        List<ItemDto> itemDtoList = new ArrayList<>();
        for (Item item : itemList) {
            itemDtoList.add(ItemMapper.toItemDto(item));
        }
        return itemDtoList;
    }
}
