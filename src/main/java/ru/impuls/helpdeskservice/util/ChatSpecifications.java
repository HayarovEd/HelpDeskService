package ru.impuls.helpdeskservice.util;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import ru.impuls.helpdeskservice.auth.entity.User;
import ru.impuls.helpdeskservice.entity.Chat;
import ru.impuls.helpdeskservice.entity.Connection;
import ru.impuls.helpdeskservice.entity.Message;

import java.time.Instant;
import java.util.UUID;

public class ChatSpecifications {

    public static Specification<Chat> hasSubgroupId(UUID subgroupId) {
        return (root, query, cb) ->
                cb.equal(root.get("subgroup").get("id"), subgroupId);
    }

    public static Specification<Chat> hasGroupId(UUID groupId) {
        return (root, query, cb) ->
                cb.equal(root.get("subgroup").get("group").get("id"), groupId);
    }

    public static Specification<Chat> hasLastMessageBefore(Instant to) {
        return (root, query, cb) -> {
            assert query != null;
            Subquery<Instant> subquery = query.subquery(Instant.class);
            Root<Connection> connectionRoot = subquery.from(Connection.class);
            Join<Connection, Message> messageJoin = connectionRoot.join("messages");

            subquery.select(cb.greatest(messageJoin.<Instant>get("createdDate")))
                    .where(cb.equal(connectionRoot.get("chat"), root));

            return cb.lessThan(subquery, to);
        };
    }

    public static Specification<Chat> matchesSearch(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("phone")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern)
                );
    }

    public static Specification<Chat> isOpen(boolean isOpen) {
        return (root, query, cb) ->
                cb.equal(root.get("open"), isOpen);
    }

    public static Specification<Chat> isWork(boolean isWork) {
        return (root, query, cb) ->
                cb.equal(root.get("work"), isWork);
    }

    public static Specification<Chat> hasUser(User user) {
        return (root, query, cb) -> {
            assert query != null;
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<Connection> connectionRoot = subquery.from(Connection.class);

            subquery.select(connectionRoot.get("chat").get("id"))
                    .where(cb.equal(connectionRoot.get("user"), user));

            return cb.in(root.get("id")).value(subquery);
        };
    }
}