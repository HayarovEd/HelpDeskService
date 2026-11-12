package ru.impuls.helpdeskservice.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyProviderUtil {

    private static byte[] readKeyBytes(String filePath) throws Exception {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Key file path cannot be null or empty");
        }

        if (filePath.startsWith("classpath:")) {
            String path = filePath.substring("classpath:".length()).trim();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            try (InputStream is = KeyProviderUtil.class.getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    return is.readAllBytes();
                }
            }
            throw new NoSuchFileException("Resource not found on classpath: " + filePath);
        }

        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }

        // Fallback: check classpath if path was relative to project/resources
        String normalized = filePath.replace("./src/main/resources/", "")
                .replace("src/main/resources/", "")
                .replace("./", "");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        try (InputStream is = KeyProviderUtil.class.getClassLoader().getResourceAsStream(normalized)) {
            if (is != null) {
                return is.readAllBytes();
            }
        }

        throw new NoSuchFileException("Key file not found: " + filePath);
    }

    public static PublicKey getPublicKey(String filePath) throws Exception {
        String publicKeyPEM = new String(readKeyBytes(filePath))
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey getPrivateKey(String filePath) throws Exception {
        String privateKeyPEM = new String(readKeyBytes(filePath))
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return keyFactory.generatePrivate(keySpec);
    }
}

