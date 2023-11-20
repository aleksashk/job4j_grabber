package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.utils.DateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class HabrCareerParse implements DateTimeParser {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";

    public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();
        String delimiter = "-----------------------------------";
        String msg = "Page N";
        int numberOfPage = Integer.parseInt(properties.getProperty("habr.number.pages", "1"));
        for (int i = 1; i <= numberOfPage; i++) {
            System.out.printf("%s%s%d%s%n", delimiter, msg, i, delimiter);
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, i, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").first();
                String vacancyDate = dateElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, vacancyDate, link);
            });
        }
    }

    @Override
    public LocalDateTime parse(String parse) {
        try {
            return LocalDateTime.parse(parse, ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Не удалось разобрать дату: " + parse, e);
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = HabrCareerParse.class.getClassLoader().getResourceAsStream("habrcareer.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
