package es.uca.gamebox.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Genre {

    public Genre(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    private String name;

    @ManyToMany
    @JoinTable(
            name = "game_genre",
            joinColumns = @JoinColumn(name = "genre_id"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private List<Game> games;

}
