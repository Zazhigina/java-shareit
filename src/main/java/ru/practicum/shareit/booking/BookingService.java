package ru.practicum.shareit.booking;

import org.hibernate.ObjectNotFoundException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;


public interface BookingService {
    BookingDtoOut add(Long userId,  BookingDto bookingDto) throws ObjectNotFoundException;
    BookingDtoOut update (Long userId, Long bookingId, Boolean approved);
    BookingDtoOut getBookingById (Long userId, Long bookingId);
    BookingDtoOut getAll(Long userId, String state);
    BookingDtoOut getAllOwner(Long userId, String state);
}
