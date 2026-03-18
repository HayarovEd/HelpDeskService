package ru.impuls.helpdeskservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Текущая директория: " + System.getProperty("user.dir"));
            System.out.println("Пытаюсь загрузить .env из: ./");

            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .load();

            System.out.println(".env успешно загружен!");

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
                System.out.println("Установлено свойство: " + entry.getKey());
            });

            SpringApplication.run(Main.class, args);

        } catch (Exception e) {
            System.err.println("ОШИБКА при загрузке .env: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
