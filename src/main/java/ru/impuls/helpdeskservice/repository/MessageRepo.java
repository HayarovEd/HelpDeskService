package ru.impuls.helpdeskservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.impuls.helpdeskservice.entity.Message;

import javax.swing.text.html.Option;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepo extends JpaRepository<Message, UUID> {

    @Query("select min(m.createdDate) from Message m where m.connection.chat.id = :chatId")
    Optional<Instant> findFirstCreatedDateByChatId(UUID chatId);

    @Query("select m from Message m where m.connection.chat.id = :chatId order by m.createdDate desc")
    List<Message> findMessagesByChatId(UUID chatId, Pageable pageable);

    @Query("select m from Message m where m.connection.chat.id = :chatId and m.createdDate < :to order by m.createdDate desc")
    List<Message> findMessagesByChatIdBefore(UUID chatId, Instant to, Pageable pageable);

}
