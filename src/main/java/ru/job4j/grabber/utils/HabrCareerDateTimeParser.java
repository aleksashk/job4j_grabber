package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class HabrCareerDateTimeParser implements DateTimeParser {
    private static final DateTimeFormatter INPUT_FORMATTER = ISO_OFFSET_DATE_TIME;

    @Override
    public LocalDateTime parse(String parse) {
        try {
            return LocalDateTime.parse(parse, INPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Не удалось разобрать дату: " + parse, e);
        }
    }
}
