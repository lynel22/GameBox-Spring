package es.uca.gamebox.controller;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.security.JwtResponse;
import es.uca.gamebox.security.JwtService;
import es.uca.gamebox.security.LoginRequest;
import es.uca.gamebox.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {
            userService.createUser(user.getUsername(), user.getPassword(), user.getEmail());
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
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

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String jwt = jwtService.generateToken(user);

        return ResponseEntity.ok(new JwtResponse(jwt));
    }
}
