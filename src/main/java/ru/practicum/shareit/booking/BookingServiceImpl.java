package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoOut add(Long userId, BookingDto bookingDto) throws ObjectNotFoundException {
        User user = UserMapper.toUser(userService.getUserById(userId));
        Optional<Item> itemById = itemRepository.findById(bookingDto.getItemId());
        if (itemById.isEmpty()) {
            log.debug("Вещь с id {} не найдена.", bookingDto.getItemId());
            throw new NotFoundException(String.format("Вещь с id %s не найдена.", bookingDto.getItemId()));
        }
        Booking booking = BookingMapper.toBooking(user, itemById.get(), bookingDto);
        return BookingMapper.toBookingOut(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoOut update(Long userId, Long bookingId, Boolean approved) {
        return null;
    }

    @Override
    public BookingDtoOut getBookingById(Long userId, Long bookingId) {
        return null;
    }

    @Override
    public BookingDtoOut getAll(Long userId, String state) {
        return null;
    }

    @Override
    public BookingDtoOut getAllOwner(Long userId, String state) {
        return null;
    }
}
