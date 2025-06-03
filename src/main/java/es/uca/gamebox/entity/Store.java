package es.uca.gamebox.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class Store{
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private String slugRawg;

    @NotNull
    private int rawgId;

    @NotNull
    private String domain;

    private String imageUrl;

    @ManyToMany(mappedBy = "stores")
    private Set<Game> games = new HashSet<>();

    @NotNull
    @CreatedDate
    private Date createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
