package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private final DateTimeParser dateTimeParser;

    private int postId = 0;

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        Parse parser = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> posts = parser.list(PAGE_LINK);
        posts.forEach(System.out::println);
    }

    private static String retrieveDescription(String link) throws IOException {
        StringBuilder rsl = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".style-ugc");
        rows.forEach(row -> {
            for (int i = 0; i < row.childNodeSize(); i++) {
                rsl.append(Jsoup.parse(row.childNode(i).toString()).text());
                rsl.append(System.lineSeparator());
            }
        });
        return rsl.toString();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> rsl = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(link + "?page=" + i);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String date = dateElement.attr("datetime");
                String vacancyName = titleElement.text();
                String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description;
                try {
                    description = retrieveDescription(vacancyLink);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                rsl.add(new Post(postId, vacancyLink, vacancyName, description, dateTimeParser.parse(date)));
                postId++;
            });
        }
        return rsl;
    }
}