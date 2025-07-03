package ru.impuls.helpdeskservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.enums.SenderType;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "messages")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message extends BaseEntity {

    String name;
    String email;
    String phone;
    String content;

    @Enumerated(EnumType.STRING)
    SenderType sender;

    @ManyToOne(fetch = FetchType.LAZY)
    Conversation conversation;
}
