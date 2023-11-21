package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.extractor.Parse;
import ru.job4j.grabber.model.Post;
import ru.job4j.utils.DateTimeParser;
import ru.job4j.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HabrCareerParse implements Parse {

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        Properties properties = loadProperties();

        int numberOfPage = Integer.parseInt(properties.getProperty("habr.number.pages", "1"));
        String sourceLink = properties.getProperty("source.link", "https://career.habr.com");
        String sourcePrefix = properties.getProperty("source.prefix", "/vacancies?page=");
        String sourceSuffix = properties.getProperty("source.suffix", "&q=Java%20developer&type=all");

        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse(dateTimeParser);

        for (int i = 1; i <= numberOfPage; i++) {
            String fullLink = "%s%s%d%s".formatted(sourceLink, sourcePrefix, i, sourceSuffix);
            List<Post> posts = parser.list(fullLink);
            parser.printPosts(posts, i);
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
        try {
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
        } catch (IOException e) {
            System.err.println("Ошибка при извлечении описания вакансии: " + e.getMessage());
            return "Описание не доступно.";
        }
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();

        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");

            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                if (titleElement != null) {
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    Element dateElement = row.select("time.basic-date").first();

                    if (dateElement != null) {
                        String vacancyDate = dateElement.attr("datetime");
                        String vacancyLink = link.substring(0, link.indexOf("/vacancies")) + linkElement.attr("href");
                        Post post = new Post();
                        post.setTitle(vacancyName);
                        post.setLink(vacancyLink);
                        post.setCreated(dateTimeParser.parse(vacancyDate));

                        try {
                            String description = retrieveDescription(vacancyLink);
                            post.setDescription(description);
                        } catch (IOException e) {
                            System.err.println("Ошибка при извлечении описания вакансии: " + e.getMessage());
                        }

                        posts.add(post);
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Ошибка при подключении или парсинге страницы: " + e.getMessage());
        }

        return posts;
    }

    private void printPosts(List<Post> posts, int pageNumber) {
        System.out.println("--------------------------------------Page " + pageNumber + "----------------------------");
        posts.forEach(System.out::println);
    }
}
