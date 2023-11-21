package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";

    public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();
        String delimiter = "-----------------------------------";
        String msg = "Page N";
        String msgDescription = "Описание вакансии: ";
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
                String vacancyDate = dateElement.attr("datetime");
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, vacancyDate, link);
            });
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

    private static String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements headers = document.select(".vacancy-description__text h3");
        StringBuilder formattedDescription = new StringBuilder();
        for (Element header : headers) {
            formattedDescription.append(header.text()).append(":\n\n");

            Elements lists = header.nextElementSibling().select("li");
            for (Element listItem : lists) {
                formattedDescription.append(" - ").append(listItem.text()).append("\n");
            }
            formattedDescription.append("\n");
        }
        return formattedDescription.toString();
    }
}
