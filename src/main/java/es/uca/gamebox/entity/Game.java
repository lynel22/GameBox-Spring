package es.uca.gamebox.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Game {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    @NotNull
    private String name;
    @NotNull
    @Lob
    private String description;
    private String imageUrl;

    private String releaseDate;
    @ManyToOne
    @JoinColumn(name = "developer_id")
    private Developer developer;

    private String steamAppId;

    @ManyToMany
    @JoinTable(
            name = "game_genre",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    Set<Genre> genres;

    @ManyToMany
    @JoinTable(
            name = "game_platform",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "platform_id")
    )
    Set<Platform> platforms;

    @Column(unique = true)
    private String rawgSlug;

    @ManyToMany
    @JoinTable(
            name = "game_store",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    private Set<Store> stores = new HashSet<>();

    @NotNull
    private boolean steamAchievementsSynced = false;

    @NotNull
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedDate
    private LocalDateTime updatedAt;
}
