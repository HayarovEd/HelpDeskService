package ru.impuls.helpdeskservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversations")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation extends BaseEntity {

    boolean closed = false;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    List<Message> messages = new ArrayList<>();
}
