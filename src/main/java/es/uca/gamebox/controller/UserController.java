package es.uca.gamebox.controller;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.exception.ApiException;
import es.uca.gamebox.security.JwtResponse;
import es.uca.gamebox.security.JwtService;
import es.uca.gamebox.security.LoginRequest;
import es.uca.gamebox.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserController {
    private final UserService userService;
    @Autowired
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<?> saveUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {
        try {
            userService.createUser(username, password, email, avatar);
            return ResponseEntity.ok("User created successfully");
        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("/verify/account")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        System.out.println("Token recibido: " + token);
        userService.verifyAccount(token);
        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login attempt for user: " + loginRequest.getUsername());

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            log.info("Autenticación completada");

            UserDetails user = (UserDetails) auth.getPrincipal();

            log.info("Usuario obtenido, generando token...");

            String jwt = jwtService.generateToken(user);

            log.info("Token generado correctamente: " + jwt);

            JwtResponse jwtResponse = new JwtResponse(jwt);
            log.info("JWT Response creado: " + jwtResponse);

            return ResponseEntity.ok(jwtResponse);

        } catch (DisabledException e) {
            log.warn("Login failed: account not activated");
            return ResponseEntity.status(403).body("Tu cuenta no está activada. Por favor revisa tu correo para activarla.");
        } catch (BadCredentialsException e) {
            log.warn("Login failed: bad credentials");
            return ResponseEntity.status(401).body("Correo o contraseña incorrectos.");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR EN EL LOGIN", e);
            return ResponseEntity.status(500).body("Ha ocurrido un error inesperado.");
        }

    }

    @PostMapping("/verify/password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.forgotPassword(email);
            return ResponseEntity.ok("Password reset email sent successfully");
        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }

    }

    @PostMapping("/verify/password/submit")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token,
                                           @RequestParam("password") String password)
    {
        try {
            userService.resetPassword(token, password);
            return ResponseEntity.ok("Password reset successfully");
        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @GetMapping("profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
        }
        System.out.println("Getteando user");
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(user);
    }

    @PostMapping("profile/update")
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam(value="password", required = false) String password,
            @RequestParam(value ="avatar", required = false) MultipartFile avatar) {

        try {
            User user = (User) authentication.getPrincipal();
            userService.updateUser(user, username, password, email, avatar);
            return ResponseEntity.ok("User created successfully");
        } catch (ApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }


    }

}
