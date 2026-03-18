package ru.impuls.helpdeskservice;

import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class KeyCheckTest {
    public static void main(String[] args) {
        try {
            System.out.println("Проверка загрузки ключей...");

            // Проверяем публичный ключ
            ClassPathResource publicKeyResource = new ClassPathResource("static/security/public_key.pem");
            if (publicKeyResource.exists()) {
                System.out.println("✅ Публичный ключ найден");
                try (InputStream is = publicKeyResource.getInputStream()) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("   Первые 50 символов: " + content.substring(0, 50) + "...");
                    System.out.println("   Длина файла: " + content.length() + " символов");

                    // Проверяем формат
                    if (content.contains("BEGIN PUBLIC KEY")) {
                        System.out.println("   ✅ Формат публичного ключа правильный (BEGIN PUBLIC KEY)");
                    } else {
                        System.out.println("   ❌ Неправильный формат публичного ключа: " +
                                content.substring(0, content.indexOf('\n')));
                    }
                }
            } else {
                System.out.println("❌ Публичный ключ НЕ найден по пути: static/security/public_key.pem");
            }

            System.out.println();

            // Проверяем приватный ключ
            ClassPathResource privateKeyResource = new ClassPathResource("static/security/private_key.pem");
            if (privateKeyResource.exists()) {
                System.out.println("✅ Приватный ключ найден");
                try (InputStream is = privateKeyResource.getInputStream()) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("   Первые 50 символов: " + content.substring(0, 50) + "...");
                    System.out.println("   Длина файла: " + content.length() + " символов");

                    // Проверяем формат
                    if (content.contains("BEGIN PRIVATE KEY")) {
                        System.out.println("   ✅ Формат приватного ключа правильный (BEGIN PRIVATE KEY)");
                    } else {
                        System.out.println("   ❌ Неправильный формат приватного ключа: " +
                                content.substring(0, content.indexOf('\n')));
                    }
                }
            } else {
                System.out.println("❌ Приватный ключ НЕ найден по пути: static/security/private_key.pem");
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка при проверке ключей:");
            e.printStackTrace();
        }
    }
}