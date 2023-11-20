package ru.job4j.grabber;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HabrCareerParseTest {

    @Test
    void parseIsoFormattedDateSuccess() {
        String parse = "2023-11-10T21:41:32+03:00";
        LocalDateTime actual = LocalDateTime.parse(parse, ISO_OFFSET_DATE_TIME);
        LocalDateTime expected = LocalDateTime.of(2023, Month.NOVEMBER, 10, 21, 41, 32);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void parseIsoFormattedDateFailed() {
        String parse = "2023-11-10";
        assertThrows(DateTimeParseException.class, () -> LocalDateTime.parse(parse, ISO_OFFSET_DATE_TIME));
    }
}