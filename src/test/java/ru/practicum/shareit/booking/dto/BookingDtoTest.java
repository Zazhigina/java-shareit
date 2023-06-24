package ru.practicum.shareit.booking.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    private static final String DATE_TIME = "2023-05-13T17:33:33";

    private BookingDto bookingDto = null;

    @BeforeEach
    public void init() {
        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.parse("2023-05-13T17:33:33"))
                .end(LocalDateTime.parse("2023-05-13T17:33:33"))
                .build();
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование сериализации даты начала бронирования")
    public void startSerializes() {
        assertThat(json.write(bookingDto)).extractingJsonPathStringValue("$.start")
                .isEqualTo(DATE_TIME);
    }

    @Test
    @SneakyThrows
    @DisplayName("Тестирование сериализации даты конца бронирования")
    public void endSerializes() {
        assertThat(json.write(bookingDto)).extractingJsonPathStringValue("$.end")
                .isEqualTo(DATE_TIME);
    }
}