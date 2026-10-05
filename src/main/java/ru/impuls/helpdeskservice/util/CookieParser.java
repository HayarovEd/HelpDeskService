package ru.impuls.helpdeskservice.util;

import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.dto.internal.BillingCookie;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CookieParser {
    private static final Pattern UID_PATTERN = Pattern.compile("\"uid\";i:(\\d+)");
    private static final Pattern TIME_PATTERN = Pattern.compile("\"timelastlogin\";s:\\d+:\"([^\"]+)\"");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("i:1;s:\\d+:\"([^\"]+)\"");
    private static final Pattern DURATION_PATTERN = Pattern.compile("i:2;i:(\\d+)");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BillingCookie parse(String phpSerializedStr) {
        int uid = 0;
        LocalDateTime timeLastLogin = null;
        String sessionToken = null;
        int durationSeconds = 0;

        // 1. Извлекаем UID
        Matcher uidMatcher = UID_PATTERN.matcher(phpSerializedStr);
        if (uidMatcher.find()) {
            uid = Integer.parseInt(uidMatcher.group(1));
        }

        // 2. Извлекаем TimeLastLogin
        Matcher timeMatcher = TIME_PATTERN.matcher(phpSerializedStr);
        if (timeMatcher.find()) {
            String rawDate = timeMatcher.group(1).replace('+', ' ');
            timeLastLogin = LocalDateTime.parse(rawDate, DATE_FORMATTER);
        }

        // 3. Извлекаем Токен сессии (индекс 1)
        Matcher tokenMatcher = TOKEN_PATTERN.matcher(phpSerializedStr);
        if (tokenMatcher.find()) {
            sessionToken = tokenMatcher.group(1);
        }

        // 4. Извлекаем Длительность (индекс 2)
        Matcher durationMatcher = DURATION_PATTERN.matcher(phpSerializedStr);
        if (durationMatcher.find()) {
            durationSeconds = Integer.parseInt(durationMatcher.group(1));
        }

        return new BillingCookie(uid, timeLastLogin, sessionToken, durationSeconds);
    }
}
