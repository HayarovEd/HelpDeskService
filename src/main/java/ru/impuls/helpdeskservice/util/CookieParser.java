package ru.impuls.helpdeskservice.util;

import org.springframework.stereotype.Service;
import ru.impuls.helpdeskservice.dto.internal.BillingCookie;
import ru.impuls.helpdeskservice.dto.internal.BillingSession;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CookieParser {
    private static final Pattern UID = Pattern.compile("s:3:\"uid\";i:(\\d+);");
    private static final Pattern LOGIN = Pattern.compile("s:13:\"timelastlogin\";s:\\d+:\"([^\"]+)\";");
    private static final Pattern KEY = Pattern.compile("i:1;s:\\d+:\"([^\"]+)\";");
    private static final Pattern TTL = Pattern.compile("i:2;i:(\\d+);");
    private static final DateTimeFormatter LOGIN_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public BillingCookie parseCookies(String input) {
        Map<String, String> parts = new HashMap<>();
        for (String piece : input.split("; ")) {
            int idx = piece.indexOf('=');
            if (idx > 0) {
                parts.put(piece.substring(0, idx), piece.substring(idx + 1));
            }
        }

        String raw = parts.get("42766f398391ea63a2c99a4d4e321eb5");
        String decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8);

        int colon = decoded.indexOf(':');
        String hash = decoded.substring(0, colon);
        String serialized = decoded.substring(colon + 1);

        int userId = extractInt(UID, serialized);
        Instant timeLastLogin = parseInstant(extractStr(LOGIN, serialized));
        String key = extractStr(KEY, serialized);
        int ttl = extractInt(TTL, serialized);

        BillingSession session = new BillingSession(hash, userId, timeLastLogin, key, ttl);

        return new BillingCookie(
                parts.get("YII_CSRF_TOKEN"),
                parts.get("c8e157c1165c"),
                session
        );
    }

    private Instant parseInstant(String value) {
        return LocalDateTime.parse(value, LOGIN_FORMAT).toInstant(ZoneOffset.UTC);
    }

    private int extractInt(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (!m.find()) throw new IllegalStateException("Not found: " + p);
        return Integer.parseInt(m.group(1));
    }

    private String extractStr(Pattern p, String s) {
        Matcher m = p.matcher(s);
        if (!m.find()) throw new IllegalStateException("Not found: " + p);
        return m.group(1);
    }
}
