package ru.job4j.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class HabrCareerDateTimeParser implements DateTimeParser{
    @Override
    public LocalDateTime parse(String parse) {
        try {
            return LocalDateTime.parse(parse, ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Не удалось разобрать дату: " + parse, e);
        }
    }
}
