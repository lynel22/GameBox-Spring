package es.uca.gamebox.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(value = {"createdAt", "updatedAt", "QrCodeSecret"}, allowGetters = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    @Column(unique = true)
    private String email;
    @NotNull
    private Boolean isAdmin;
    private String imageUrl;
    private boolean isActive;

    // Multifactor authentication
    private String QrCodeSecret;
    @Column(columnDefinition = "TEXT")
    private String QrCodeImageUri;

    @NotNull
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedDate
    private LocalDateTime updatedAt;

}
