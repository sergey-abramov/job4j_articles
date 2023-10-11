package ru.job4j.articles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.articles.service.SimpleArticleService;
import ru.job4j.articles.service.generator.RandomArticleGenerator;
import ru.job4j.articles.store.ArticleStore;
import ru.job4j.articles.store.WordStore;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getSimpleName());

    public static final int TARGET_COUNT = 1_000_000;

    public static void main(String[] args) {
        var properties = loadProperties();
        var connection = initConnection(properties);
        var wordStore = new WordStore(connection);
        var articleStore = new ArticleStore(connection);
        var articleGenerator = new RandomArticleGenerator();
        var articleService = new SimpleArticleService(articleGenerator);
        articleService.generate(wordStore, TARGET_COUNT, articleStore);
    }

    private static Properties loadProperties() {
        LOGGER.info("Загрузка настроек приложения");
        var properties = new Properties();
        try (InputStream in = Application.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            properties.load(in);
        } catch (Exception e) {
            LOGGER.error("Не удалось загрузить настройки. { }", e.getCause());
            throw new IllegalStateException();
        }
        return properties;
    }

    private static Connection initConnection(Properties properties) {
        LOGGER.info("Создание подключения");
        try {
            return DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password")
            );
        } catch (SQLException throwables) {
            LOGGER.error("Не удалось выполнить операцию: { }", throwables.getCause());
            throw new IllegalStateException();
        }
    }

}
