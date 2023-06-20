package ru.practicum.shareit.booking.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingRepositoryTest {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private final User user = User.builder()
            .name("name")
            .email("email@email.com")
            .build();

    private final User owner = User.builder()
            .name("name2")
            .email("email2@email.com")
            .build();

    private final Item item = Item.builder()
            .name("name")
            .description("description")
            .available(true)
            .owner(owner)
            .build();

    private final Booking booking = Booking.builder()
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().minusHours(1L))
            .end(LocalDateTime.now().plusDays(1L))
            .build();

    private final Booking pastBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().minusDays(2L))
            .end(LocalDateTime.now().minusDays(1L))
            .build();

    private final Booking futureBooking = Booking.builder()
            .item(item)
            .booker(user)
            .status(BookingStatus.APPROVED)
            .start(LocalDateTime.now().plusDays(1L))
            .end(LocalDateTime.now().plusDays(2L))
            .build();

    @BeforeEach
    private void init() {
        testEntityManager.persist(user);
        testEntityManager.persist(owner);
        testEntityManager.persist(item);
        testEntityManager.flush();
        bookingRepository.save(booking);
        bookingRepository.save(pastBooking);
        bookingRepository.save(futureBooking);
    }

    @AfterEach
    private void deleteAll() {
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований по Id пользователя")
    void findAllByBookerId() {
        List<Booking> bookings = bookingRepository.findAllByBookerId(1L, PageRequest.of(0, 10));

        assertEquals(bookings.size(), 3);
        assertEquals(bookings.get(0).getBooker().getId(), 1L);
    }

    @Test
    @DisplayName("Тестирование получения всех текущих бронирований")
    void findAllCurrentBookingsByBookerId() {
        List<Booking> bookings = bookingRepository.findAllCurrentBookingsByBookerId(1L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getBooker().getId(), 1L);
    }

    @Test
    @DisplayName("Тестирование получения всех закончившихся бронирований")
    void findAllPastBookingsByBookerId() {
        List<Booking> bookings = bookingRepository.findAllPastBookingsByBookerId(1L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getId(), 2L);
    }

    @Test
    @DisplayName("Тестирование получения всех будущих бронирований")
    void findAllFutureBookingsByBookerId() {
        List<Booking> bookings = bookingRepository.findAllFutureBookingsByBookerId(1L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getId(), 3L);
    }

    @Test
    @DisplayName("Тестирование получения всех неподтвержденных бронирований")
    void findAllWaitingBookingsByBookerId() {
        Booking waitingBooking = Booking.builder()
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();

        bookingRepository.save(waitingBooking);
        List<Booking> bookings = bookingRepository.findAllWaitingBookingsByBookerId(1L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Тестирование получения всех отклоненных бронирований")
    void findAllRejectedBookingsByBookerId() {
        Booking rejectedBooking = Booking.builder()
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();

        bookingRepository.save(rejectedBooking);
        List<Booking> bookings = bookingRepository.findAllRejectedBookingsByBookerId(1L,
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований по Id владельца")
    void findAllByOwnerId() {
        List<Booking> bookings = bookingRepository.findAllByOwnerId(2L, PageRequest.of(0, 10));

        assertEquals(bookings.size(), 3);
    }

    @Test
    @DisplayName("Тестирование получения всех текущих бронирований по Id владельца")
    void findAllCurrentBookingsByOwnerId() {
        List<Booking> bookings = bookingRepository.findAllCurrentBookingsByOwnerId(2L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getItem().getOwner().getId(), 2L);
    }

    @Test
    @DisplayName("Тестирование получения всех закончившихся бронирований по Id владельца")
    void findAllPastBookingsByOwnerId() {
        List<Booking> bookings = bookingRepository.findAllPastBookingsByOwnerId(2L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getItem().getOwner().getId(), 2L);
    }

    @Test
    @DisplayName("Тестирование получения всех будущих бронирований по Id владельца")
    void findAllFutureBookingsByOwnerId() {
        List<Booking> bookings = bookingRepository.findAllFutureBookingsByOwnerId(2L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getItem().getOwner().getId(), 2L);
    }

    @Test
    @DisplayName("Тестирование получения всех неподтвержденных бронирований по Id владельца")
    void findAllWaitingBookingsByOwnerId() {
        Booking waitingBooking = Booking.builder()
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();

        bookingRepository.save(waitingBooking);
        List<Booking> bookings = bookingRepository.findAllWaitingBookingsByOwnerId(2L, LocalDateTime.now(),
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.WAITING);
    }

    @Test
    @DisplayName("Тестирование получения всех отклоненных бронирований по Id владельца")
    void findAllRejectedBookingsByOwnerId() {
        Booking rejectedBooking = Booking.builder()
                .item(item)
                .booker(user)
                .status(BookingStatus.REJECTED)
                .start(LocalDateTime.now().plusDays(1L))
                .end(LocalDateTime.now().plusDays(2L))
                .build();

        bookingRepository.save(rejectedBooking);
        List<Booking> bookings = bookingRepository.findAllRejectedBookingsByOwnerId(2L,
                PageRequest.of(0, 10));

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("Тестирование получения всех бронирований пользователя")
    void findAllByUserBookings() {
        List<Booking> bookings = bookingRepository.findAllByUserBookings(1L, 1L, LocalDateTime.now());

        assertEquals(bookings.size(), 1);
        assertEquals(bookings.get(0).getStatus(), BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Тестирование получения последнего бронирования")
    void getLastBooking() {
        Optional<Booking> bookingOptional = bookingRepository.getLastBooking(1L, LocalDateTime.now());
        Booking actualBooking;

        if (bookingOptional.isPresent()) {
            actualBooking = bookingOptional.get();

            assertEquals(actualBooking.getId(), 1L);
        } else {

            fail();
        }
    }

    @Test
    @DisplayName("Тестирование получения следующего бронирования")
    void getNextBooking() {
        Optional<Booking> bookingOptional = bookingRepository.getNextBooking(1L, LocalDateTime.now());
        Booking actualBooking;

        if (bookingOptional.isPresent()) {
            actualBooking = bookingOptional.get();

            assertEquals(actualBooking.getId(), 3L);
        } else {

            fail();
        }
    }
}