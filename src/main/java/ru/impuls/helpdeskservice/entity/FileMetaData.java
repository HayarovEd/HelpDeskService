package ru.impuls.helpdeskservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class FileMetaData  extends BaseEntity{
    @Column(name = "original_file_name")
    String originalFileName;
    
    @Column(name = "stored_file_name")
    String storedFileName;
    
    @Column(name = "file_size")
    Long fileSize;
    
    @Column(name = "mime_type")
    String mimeType;

    @CreatedDate
    @Column(name = "created_date")
    Instant createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    Message message;
}
