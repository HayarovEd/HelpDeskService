package ru.impuls.helpdeskservice.util;

import java.security.SecureRandom;

public class TokenGeneratorUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    // Символы для генерации сложного токена
    private static final String ALPHA_NUMERIC =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Генерация случайного числового токена
     * @param length длина токена в символах
     * @return строковое представление токена (только цифры)
     */
    public static String generateNumericToken(int length) {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < length; i++) {
            token.append(secureRandom.nextInt(10));
        }
        return token.toString();
    }

    /**
     * Генерация сложного токена из цифр и букв
     * @param length длина токена в символах
     * @return строковое представление токена (цифры + буквы)
     */
    public static String generateAlphaNumericToken(int length) {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(ALPHA_NUMERIC.length());
            token.append(ALPHA_NUMERIC.charAt(index));
        }
        return token.toString();
    }
}
