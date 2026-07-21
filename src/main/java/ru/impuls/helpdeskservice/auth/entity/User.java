package ru.impuls.helpdeskservice.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.impuls.helpdeskservice.entity.BaseEntity;
import ru.impuls.helpdeskservice.entity.FireBaseToken;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    String name;
    String username;
    String email;
    String password;
    @Column(name = "token_version")
    String tokenVersion;

    @Column(name = "is_disabled")
    boolean disabled;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Singular
    List<Token> tokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Singular
    List<FireBaseToken> firebaseTokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.emptyList(); //TODO
        }

        return Collections.singletonList(
                new SimpleGrantedAuthority(role.getName())
        );
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return !disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        if (!super.equals(o)) return false;
        return disabled == user.disabled &&
                Objects.equals(username, user.username) &&
                Objects.equals(tokenVersion, user.tokenVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, disabled, tokenVersion);
    }
}
