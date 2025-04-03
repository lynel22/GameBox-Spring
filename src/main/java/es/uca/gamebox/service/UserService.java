package es.uca.gamebox.service;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.exception.ApiException;
import es.uca.gamebox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    public void createUser(String username, String password, String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new ApiException("El email ya está registrado");
        }
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setIsAdmin(false);
        user.setEnabled(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setQrCodeSecret(null);
        user.setQrCodeImageUri(null);

        userRepository.save(user);

        // Send a verification email
        emailService.sendNewAccountEmail(username, email, token);

        log.info("User created: {}", username);
    }

    public void verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setEnabled(true);
        user.setVerificationToken(null); // Eliminamos el token tras la verificación
        userRepository.save(user);
    }
}
