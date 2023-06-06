package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.booking.model.BookingState;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOut create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @Valid @RequestBody BookingDto bookingDto) {
        log.info("POST запрос на создание нового бронирования вещи: {} от пользователя c id: {}", bookingDto, userId);
        return bookingService.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut updateStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                      @PathVariable("bookingId")
                                   Long bookingId,
                                      @RequestParam(name = "approved") Boolean approved) {
        log.info("PATCH запрос на обновление статуса бронирования вещи : {} от владельца с id: {}", bookingId, userId);
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @PathVariable("bookingId")
                                     Long bookingId) {
        log.info("GET запрос на получение данных о конкретном бронировании {} от пользователся с id: {}", bookingId, userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public BookingDtoOut getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @RequestParam(value = "state", defaultValue = "ALL") String bookingState) {
        BookingState state = BookingState.from(bookingState);
        if (Objects.isNull(state)) {
            throw new IllegalArgumentException(String.format("Неизвестный статус: %s", bookingState));
        }
        log.info("GET запрос на получениесписка всех бронирований текущего пользователя с id: {} и статусом {}", userId, state);
        return bookingService.getAll(userId, bookingState);
    }

    @GetMapping("/owner")
    public BookingDtoOut getAllOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                     @RequestParam(value = "state", defaultValue = "ALL") String bookingState) {
        BookingState state = BookingState.from(bookingState);
        if (Objects.isNull(state)) {
            throw new IllegalArgumentException(String.format("Неизвестный статус: %s", bookingState));
        }
        log.info("GET запрос на получениесписка всех бронирований текущего владельца с id: {} и статусом {}", ownerId, state);
        return bookingService.getAllOwner(ownerId, bookingState);
    }
}
