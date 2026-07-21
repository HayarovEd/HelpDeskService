package ru.impuls.helpdeskservice.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseInitializer {

    @Value("${app.firebase-configuration-file:impuls_help_desk.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing Firebase SDK...");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource(firebaseConfigPath).getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase app...");
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized!");
            } else {
                log.info("Firebase app is already initialized, skipping initialization.");
            }
        } catch (IOException e) {
            log.error("❌ Firebase initialization failed: {}", e.getMessage());
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
