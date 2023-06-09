package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        UserDto user = userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(UserMapper.toUser(user));
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        UserDto user = userService.getUserById(userId);
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isPresent()) {
            if (!itemOptional.get().getOwner().getId().equals(userId)) {
                log.debug("Пользователь с id {} не является владельцем предмета под id {}", userId, itemId);
                throw new NotFoundException(String.format("Пользователь с id %s " +
                        "не является владельцем предмета id %s.", userId, itemId));
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

            return ItemMapper.toItemDto(itemRepository.save(item));
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Long userId, Long itemId) {
        userService.getUserById(userId);
        Optional<Item> itemGet = itemRepository.findById(itemId);
        if (itemGet.isEmpty()) {
            log.debug("У пользователя с id {} не существует предмета с id {}", userId, itemId);
            throw new ru.practicum.shareit.exceptions.NotFoundException(String.format("У пользователя с id %s не " +
                    "существует предмета с id %s", userId, itemId));
        }
        Item item = itemGet.get();

        ItemDto itemDto = ItemMapper.toItemDto(item);
        itemDto.setComments(getAllComments(itemId));
        if (!item.getOwner().getId().equals(userId)) {
            return itemDto;
        }

        getLastBooking(itemDto);
        getNextBooking(itemDto);

        return itemDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAll(Long userId) {
        userService.getUserById(userId);

        List<ItemDto> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        items.forEach(i -> {
            getLastBooking(i);
            getNextBooking(i);
            i.setComments(getAllComments(i.getId()));
        });

        return items;
    }

    @Transactional
    public List<CommentDto> getAllComments(Long itemId) {
        List<Comment> comments = commentRepository.findAllByItemId(itemId);

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(Long userId, String text) {
        userService.getUserById(userId);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return ItemMapper.mapToItemDto(itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, CommentDto commentDto, Long itemId) {
        User user = UserMapper.toUser(userService.getUserById(userId));

        Optional<Item> itemById = itemRepository.findById(itemId);

        if (itemById.isEmpty()) {
            log.debug("Пользователь с id {} нет предмета с id {}.", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id: %s " +
                    "нет предмета с id: %s.", userId, itemId));
        }
        Item item = itemById.get();

        List<Booking> userBookings = bookingRepository.findAllByUserBookings(userId, itemId, LocalDateTime.now());

        if (userBookings.isEmpty()) {
            log.debug("Пользователь с id {} должен иметь хотя бы одно бронирование предмета с id {}.", userId, itemId);
            throw new ValidationException(String.format("Пользователь с id %s должно быть хотя бы одно бронирование " +
                    "предмета с id %s.", userId, itemId));
        }

        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, item, user)));
    }

    private void getLastBooking(ItemDto itemDto) {
        Optional<Booking> lastBooking = bookingRepository.getLastBooking(itemDto.getId(), LocalDateTime.now());
        if (lastBooking.isPresent()) {
            itemDto.setLastBooking(BookingMapper.toBookingItemDto(lastBooking.get()));
        } else {
            itemDto.setLastBooking(null);
        }
    }

    private void getNextBooking(ItemDto itemDto) {
        Optional<Booking> nextBooking = bookingRepository.getNextBooking(itemDto.getId(), LocalDateTime.now());
        if (nextBooking.isPresent()) {
            itemDto.setNextBooking(BookingMapper.toBookingItemDto(nextBooking.get()));
        } else {
            itemDto.setNextBooking(null);
        }

    }
}