package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.RequestMapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final User user = User.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("username")
            .email("email@email.com")
            .build();

    private final Item item = Item.builder()
            .id(1L)
            .name("item name")
            .description("description")
            .available(true)
            .owner(user)
            .build();

    private final ItemRequest request = ItemRequest.builder()
            .id(1L)
            .description("request description")
            .items(List.of(item))
            .build();

    @Test
    @DisplayName("Тестирование добавления запроса")
    void addNewRequest() {
        ItemRequestDto expectedRequestDto = RequestMapping.toRequestDto(request);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto actualRequestDto = requestService.add(user.getId(), expectedRequestDto);

        assertEquals(expectedRequestDto, actualRequestDto);
    }

    @Test
    @DisplayName("Тестирование получения запросов пользователя")
    void getUserRequests() {
        List<ItemRequestDto> expectedRequestsDto = List.of(RequestMapping.toRequestDto(request));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findAllByRequesterId(userDto.getId())).thenReturn(List.of(request));

        List<ItemRequestDto> actualRequestsDto = requestService.getUserRequests(userDto.getId());

        assertEquals(expectedRequestsDto, actualRequestsDto);
    }

    @Test
    @DisplayName("Тестирование получения всех запросов")
    void getAllRequests() {
        List<ItemRequestDto> expectedRequestsDto = List.of(RequestMapping.toRequestDto(request));
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findAllByRequester_IdNotOrderByCreatedDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of( request));

        List<ItemRequestDto> actualRequestsDto = requestService.getAllRequests(userDto.getId(), 0, 10);

        assertEquals(expectedRequestsDto, actualRequestsDto);
    }

    @Test
    @DisplayName("Тестирование получения запроса по Id")
    void getRequestById() {
        ItemRequestDto expectedRequestDto = RequestMapping.toRequestDto(request);
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));

        ItemRequestDto actualRequestDto = requestService.getRequestById(userDto.getId(), request.getId());

        assertEquals(expectedRequestDto, actualRequestDto);
    }

    @Test
    @DisplayName("Тестирование получения запроса по несуществующему Id")
    void getRequestById_whenRequestIdIsNotValid_thenThrowObjectNotFoundException() {
        when(userService.getUserById(user.getId())).thenReturn(userDto);
        when(requestRepository.findById(request.getId())).thenReturn(Optional.empty());

        NotFoundException requestNotFoundException = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(userDto.getId(), request.getId()));

        assertEquals(requestNotFoundException.getMessage(), String.format("Запрос с id: %s" +
                " не был найден.", request.getId()));
    }
}