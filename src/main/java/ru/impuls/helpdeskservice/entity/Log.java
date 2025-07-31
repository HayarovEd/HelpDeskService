package ru.impuls.helpdeskservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.auth.entity.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "logs")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Log extends BaseEntity {

    String text;
    Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;
}
