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
            log.info("Initializing Firebase SDK with config: {}...", firebaseConfigPath);
            var resource = new ClassPathResource(firebaseConfigPath);
            if (!resource.exists()) {
                log.warn("⚠️ Firebase configuration file not found: {}. Firebase features will be disabled.", firebaseConfigPath);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("Initializing Firebase app...");
                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully!");
            } else {
                log.info("Firebase app is already initialized, skipping initialization.");
            }
        } catch (Exception e) {
            log.error("❌ Firebase initialization failed: {}", e.getMessage());
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("⚠️ FirebaseApp is not initialized. FirebaseMessaging will not be available.");
            return null;
        }
        return FirebaseMessaging.getInstance();
    }
}
