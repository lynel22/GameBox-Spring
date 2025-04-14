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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonIgnoreProperties(value = {"createdAt", "updatedAt", "QrCodeSecret"}, allowGetters = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    @NotNull
    @Column(unique = true)
    private String username;
    @NotNull
    private String password;
    @NotNull
    @Column(unique = true)
    private String email;
    @NotNull
    private Boolean isAdmin;
    private String imageUrl;

    @Column(nullable = false)
    private boolean enabled = false;
    private String verificationToken;
    private String passwordResetToken;


    // Multifactor authentication
    private String QrCodeSecret;
    @Column(columnDefinition = "TEXT")
    private String QrCodeImageUri;

    @NotNull
    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedDate
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.isAdmin) {
            return List.of(() -> "ROLE_ADMIN");
        } else {
            return List.of(() -> "ROLE_USER");
        }
    }

    public String getName() {
        return this.username;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
