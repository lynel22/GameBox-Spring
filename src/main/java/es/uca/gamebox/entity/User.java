package es.uca.gamebox.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonIgnore
    @Setter
    @Getter(onMethod = @__({@JsonIgnore}))
    private String password;
    @NotNull
    @Column(unique = true)
    private String email;
    @NotNull
    @Getter(onMethod = @__({@JsonIgnore}))
    private Boolean isAdmin;
    private String imageUrl;


    @Column(nullable = false)
    private boolean enabled = false;
    @Getter(onMethod = @__({@JsonIgnore}))
    private String verificationToken;
    @Getter(onMethod = @__({@JsonIgnore}))
    private String passwordResetToken;

    // Multifactor authentication
    @Getter(onMethod = @__({@JsonIgnore}))
    private String QrCodeSecret;
    @Column(columnDefinition = "TEXT")
    @Getter(onMethod = @__({@JsonIgnore}))
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

    @JsonProperty("username")
    public String getRealUserName() {
        return this.username;
    }

    @JsonIgnore
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
