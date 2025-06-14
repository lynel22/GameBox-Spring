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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"}, allowGetters = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Library {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    private String Name;

    @NotNull
    @ManyToOne(optional = false)
    private Store store;

    @OneToMany(mappedBy = "library", fetch = FetchType.LAZY)
    private List<GameUser> gameUsers = new ArrayList<>();


    @NotNull
    @CreatedDate
    private Date createdAt;
    @CreatedDate
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    private User user;

}
