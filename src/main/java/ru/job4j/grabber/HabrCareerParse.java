package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final int PAGE_COUNT = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        StringBuilder rsl = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        try {
            Document document = connection.get();
            Elements rows = document.select(".style-ugc");
            rows.forEach(row -> {
                for (int i = 0; i < row.childNodeSize(); i++) {
                    rsl.append(Jsoup.parse(row.childNode(i).toString()).text());
                    rsl.append(System.lineSeparator());
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
        return rsl.toString();
    }

    private Post getPost(Element row, String link) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = row.select(".vacancy-card__date").first().child(0);
        String date = dateElement.attr("datetime");
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", link, linkElement.attr("href"));
        String description;
        description = retrieveDescription(vacancyLink);
        return new Post(vacancyLink, vacancyName, description, dateTimeParser.parse(date));
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGE_COUNT; i++) {
            Connection connection = Jsoup.connect(link + "?page=" + i);
            Document document;
            try {
                document = connection.get();
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> posts.add(getPost(row, link)));
        }
        return posts;
    }
}