package es.uca.gamebox.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    private String username;
    private String password;

    // Constructor vacío (necesario para deserialización)
    public LoginRequest() {}
}