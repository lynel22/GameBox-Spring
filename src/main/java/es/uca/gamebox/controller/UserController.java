package es.uca.gamebox.controller;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.security.JwtResponse;
import es.uca.gamebox.security.JwtService;
import es.uca.gamebox.security.LoginRequest;
import es.uca.gamebox.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }

    @GetMapping("/verify/account")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try{
            log.info("Login attempt for user: " + loginRequest.getUsername() + " with password: " + loginRequest.getPassword());

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails user = (UserDetails) auth.getPrincipal();
            String jwt = jwtService.generateToken(user);

            return ResponseEntity.ok(new JwtResponse(jwt));
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
