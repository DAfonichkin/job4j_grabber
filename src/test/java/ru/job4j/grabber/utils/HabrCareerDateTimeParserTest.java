package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class HabrCareerDateTimeParserTest {

    private final DateTimeParser parser = new HabrCareerDateTimeParser();

    @Test
    void whenParseCurrentTimeFromStringThenGetCurrentTime() {
        ZonedDateTime currentDate = ZonedDateTime.now();
        String currentDateString = currentDate.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        assertThat(parser.parse(currentDateString)).isEqualTo(currentDate.toLocalDateTime());
    }

    @Test
    void whenParseStringToLocalDateTime() {
        LocalDateTime date = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        String dateString = "2022-01-01T00:00:00+03:00";
        assertThat(parser.parse(dateString)).isEqualTo(date);
    }
}