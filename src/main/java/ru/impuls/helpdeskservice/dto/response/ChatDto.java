package ru.impuls.helpdeskservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import ru.impuls.helpdeskservice.dto.BaseDto;
import ru.impuls.helpdeskservice.dto.internal.ConnectionDto;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatDto extends BaseDto {
    String name;
    String login;
    String email;
    String phone;

    @JsonProperty("is_open")
    boolean open;

    @JsonProperty("is_work")
    boolean work;

    @JsonProperty("is_active")
    boolean active;

    @JsonProperty("unread_messages_count")
    long unreadMessagesCount;

    @JsonProperty("created_date")
    Instant createdDate;
    GroupResponse group;
    List<ConnectionDto> connections;
}
