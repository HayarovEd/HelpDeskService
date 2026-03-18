package ru.impuls.helpdeskservice;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class GenerateRsaKeys {
    public static void main(String[] args) {
        try {
            System.out.println("Генерация RSA ключей для JWT...");

            // Создаем папку static/security
            File keyDir = new File("src/main/resources/static/security");
            if (!keyDir.exists()) {
                boolean created = keyDir.mkdirs();
                if (created) {
                    System.out.println("✅ Создана папка: " + keyDir.getAbsolutePath());
                }
            }

            // Генерируем ключи
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            // Сохраняем приватный ключ в правильном формате PKCS8
            PrivateKey privateKey = pair.getPrivate();
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKey.getEncoded()) +
                    "\n-----END PRIVATE KEY-----";

            Files.write(Paths.get("src/main/resources/static/security/private_key.pem"), privateKeyPem.getBytes());
            System.out.println("✅ Приватный ключ создан: src/main/resources/static/security/private_key.pem");

            // Сохраняем публичный ключ
            PublicKey publicKey = pair.getPublic();
            String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey.getEncoded()) +
                    "\n-----END PUBLIC KEY-----";

            Files.write(Paths.get("src/main/resources/static/security/public_key.pem"), publicKeyPem.getBytes());
            System.out.println("✅ Публичный ключ создан: src/main/resources/static/security/public_key.pem");

            // Проверяем созданные файлы
            System.out.println("\n✅ Ключи успешно созданы!");
            System.out.println("   Размер приватного ключа: " +
                    Files.size(Paths.get("src/main/resources/static/security/private_key.pem")) + " байт");
            System.out.println("   Размер публичного ключа: " +
                    Files.size(Paths.get("src/main/resources/static/security/public_key.pem")) + " байт");

        } catch (Exception e) {
            System.err.println("❌ Ошибка при генерации ключей:");
            e.printStackTrace();
        }
    }
}