package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    @SneakyThrows
    @DisplayName("Тестирование добавления вещи прошедшей валидацию")
    void createItem_whenItemIsValid_thenReturnStatusOk() {
        Long userId = 0L;
        ItemDto itemDtoToCreate = ItemDto.builder()
                .description("some item description")
                .name("some item name")
                .available(true)
                .build();

        when(itemService.create(userId, itemDtoToCreate)).thenReturn(itemDtoToCreate);

        String result = mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDtoToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDtoToCreate), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование добавления вещи не прошедшей валидацию")
    void createItem_whenItemIsNotValid_thenReturnBadRequest() {
        Long userId = 0L;
        ItemDto itemDtoToCreate = ItemDto.builder()
                .description(" ")
                .name(" ")
                .available(null)
                .build();

        when(itemService.create(userId, itemDtoToCreate)).thenReturn(itemDtoToCreate);

        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDtoToCreate)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).create(userId, itemDtoToCreate);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование обновления вещи прошедшей валидацию")
    void update_whenItemIsValid_thenReturnStatusIsOk() {
        Long itemId = 0L;
        Long userId = 0L;
        ItemDto itemDtoToCreate = ItemDto.builder()
                .id(itemId)
                .description("some item description")
                .name("some item name")
                .available(true)
                .build();

        when(itemService.update(userId, itemId, itemDtoToCreate)).thenReturn(itemDtoToCreate);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDtoToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDtoToCreate), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование получения вещи по Id")
    void get_thenReturnStatusOk() {
        Long itemId = 0L;
        Long userId = 0L;
        ItemDto itemDtoToCreate = ItemDto.builder()
                .id(itemId)
                .description("")
                .name("")
                .available(true)
                .build();

        when(itemService.getItemById(userId, itemId)).thenReturn(itemDtoToCreate);

        String result = mockMvc.perform(MockMvcRequestBuilders.get("/items/{itemId}", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(itemDtoToCreate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDtoToCreate), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование получения всех вещей")
    void getAll_thenReturnStatusOk() {
        Long userId = 0L;
        Integer from = 0;
        Integer size = 10;
        List<ItemDto> itemsDtoToExpect = List.of(ItemDto.builder()
                .name("some item name")
                .description("some item description")
                .available(true)
                .build());

        when(itemService.getAll(userId, from, size)).thenReturn(itemsDtoToExpect);

        String result = mockMvc.perform(MockMvcRequestBuilders.get("/items", from, size)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemsDtoToExpect), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование поиска вещи по тексту")
    void searchItems_thenReturnStatusOk() {
        Long userId = 0L;
        Integer from = 0;
        Integer size = 10;
        String text = "find";
        List<ItemDto> itemsDtoToExpect = List.of(ItemDto.builder()
                .name("some item name")
                .description("some item description")
                .available(true)
                .build());

        when(itemService.search(userId, text, from, size)).thenReturn(itemsDtoToExpect);

        String result = mockMvc.perform(MockMvcRequestBuilders.get("/items/search", from, size)
                        .header("X-Sharer-User-Id", userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemsDtoToExpect), result);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование добавления комментария к вещи по Id")
    void createComment_whenCommentIsValid_thenReturnStatusIsOk() {
        Long itemId = 0L;
        Long userId = 0L;
        CommentDto commentToAdd = CommentDto.builder()
                .text("some comment")
                .created(LocalDateTime.now())
                .build();

        when(itemService.createComment(userId, commentToAdd, itemId)).thenReturn(commentToAdd);

        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(commentToAdd)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(commentToAdd), result);
    }
}