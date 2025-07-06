package ru.impuls.helpdeskservice.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.constant.TokenType;
import ru.impuls.helpdeskservice.entity.BaseEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tokens")
public class Token extends BaseEntity {

    String token;
    Instant createdAt;
    Instant expiresAt;

    @Enumerated(EnumType.STRING)
    TokenType tokenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
