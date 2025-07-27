package es.uca.gamebox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GameReview {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    @ManyToOne(optional = false)
    private User user;

    @NotNull
    @ManyToOne(optional = false)
    private Game game;

    @NotNull
    private boolean recommended;

    private LocalDateTime createdAt = LocalDateTime.now();
}
