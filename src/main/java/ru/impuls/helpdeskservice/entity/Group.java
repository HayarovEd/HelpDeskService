package ru.impuls.helpdeskservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "groups")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Group extends BaseEntity {
    String name;
    @Column(name = "oper_id")
    Long operId;

   /* @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    List<Subgroup> subgroups = new ArrayList<>();*/
}
