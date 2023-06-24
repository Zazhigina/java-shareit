package ru.practicum.shareit.item.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceIT {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private RequestService requestService;

    @Autowired
    private BookingService bookingService;

    private final UserDto userDto1 = UserDto.builder()
            .name("name1")
            .email("email1@email.com")
            .build();

    private final UserDto userDto2 = UserDto.builder()
            .name("name2")
            .email("email2@email.com")
            .build();

    private final ItemDto itemDto1 = ItemDto.builder()
            .name("item1 name")
            .description("item1 description")
            .available(true)
            .build();

    private final ItemDto itemDto2 = ItemDto.builder()
            .name("item2 name")
            .description("item2 description")
            .available(true)
            .build();

    private final ItemDto itemDtoRequest = ItemDto.builder()
            .name("itemDtoRequest name")
            .description("itemDtoRequest description")
            .available(true)
            .requestId(1L)
            .build();

    private final ItemRequestDto requestDto = ItemRequestDto.builder()
            .id(1L)
            .description("request description")
            .build();

    private final BookingDto bookingDto = BookingDto.builder()
            .itemId(1L)
            .start(LocalDateTime.now().plusSeconds(20L))
            .end(LocalDateTime.now().plusSeconds(21L))
            .build();

    private final CommentDto commentDto = CommentDto.builder()
            .text("comment text")
            .build();

    @Test
    @DisplayName("Интеграционное тестирование добавления вещи")
    void addNewItem() {
        UserDto addedUser = userService.add(userDto1);
        ItemDto addedItem = itemService.create(addedUser.getId(), itemDto1);

        assertEquals(1L, addedItem.getId());
        assertEquals("item1 name", addedItem.getName());
    }

    @Test
    @DisplayName("Интеграционное тестирование добавления вещи после запроса")
    void addRequestItem() {
        UserDto addedUser = userService.add(userDto1);
        requestService.add(addedUser.getId(), requestDto);

        ItemDto addedItemRequest = itemService.create(addedUser.getId(), itemDtoRequest);

        assertEquals(1L, addedItemRequest.getRequestId());
        assertEquals("itemDtoRequest name", addedItemRequest.getName());
    }

    @Test
    @SneakyThrows
    @DisplayName("Интеграционное тестирование добавления комментария")
    void addCommentItem() {
        UserDto addedUser1 = userService.add(userDto1);
        UserDto addedUser2 = userService.add(userDto2);
        ItemDto addedItem = itemService.create(addedUser2.getId(), itemDto2);
        BookingDtoOut bookingDtoOut = bookingService.add(addedUser1.getId(), bookingDto);

        bookingService.update(addedUser2.getId(), bookingDtoOut.getId(), true);
        Thread.sleep(12000);
        CommentDto addedComment = itemService.createComment(addedUser1.getId(), commentDto, addedItem.getId());

        assertEquals(1L, addedComment.getId());
        assertEquals("comment text", addedComment.getText());
    }

    @Test
    @DisplayName("Интеграционное тестирование получения вещи по несуществующему Id")
    void getItemById_whenItemIdIsNotValid_thenThrowRuntimeException() {
        Long itemId = 3L;

        Assertions
                .assertThrows(RuntimeException.class,
                        () -> itemService.getItemById(userDto1.getId(), itemId));
    }
}