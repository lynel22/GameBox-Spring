package es.uca.gamebox.service;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.exception.ApiException;
import es.uca.gamebox.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public void createUser(String username, String password, String email, MultipartFile avatar) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new ApiException("El email ya está registrado");
        }
        Optional<User> existingUsername = userRepository.findByUsername(username);
        if (existingUsername.isPresent()) {
            throw new ApiException("El nombre de usuario ya está en uso");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setIsAdmin(false);
        user.setEnabled(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setQrCodeSecret(null);
        user.setQrCodeImageUri(null);

        if (avatar != null && !avatar.isEmpty()) {
            // Crear ruta única para el avatar
            storeImg(user, avatar);
        }

        userRepository.save(user);

        // Send a verification email
        emailService.sendNewAccountEmail(username, email, token);

        log.info("User created: {}", username);
    }

    public void updateUser(User user, String username, String password, String email, MultipartFile avatar) {
        if (username != null){
            user.setUsername(username);
        }
        if (password != null){
            user.setPassword(passwordEncoder.encode(password));
        }
        if (email != null){
            user.setEmail(email);
        }

        if (avatar != null && !avatar.isEmpty()) {
            // Eliminar el avatar anterior si existe
            if (user.getImageUrl() != null) {
                try {
                    Path oldAvatarPath = Paths.get("src/main/resources/static" + user.getImageUrl());
                    Files.deleteIfExists(oldAvatarPath);
                } catch (Exception e) {
                    throw new ApiException("Error al eliminar el avatar anterior: " + e.getMessage());
                }
            }
            storeImg(user, avatar);
        }

        userRepository.save(user);
        log.info("User updated: {}", username);
    }

    private void storeImg(User user, MultipartFile avatar) {
        String fileName = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
        Path uploadPath = Paths.get("src/main/resources/static/uploads/avatars");

        if (!Files.exists(uploadPath)) {
            try{
                Files.createDirectories(uploadPath);
            } catch (Exception e) {
                throw new ApiException("Error al crear el directorio de subida: " + e.getMessage());
            }
        }

        Path filePath = uploadPath.resolve(fileName);
        try{
            Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (Exception e){
            throw new ApiException("Error al guardar el avatar: " + e.getMessage());
        }

        user.setImageUrl("/uploads/avatars/" + fileName);
    }

    public void verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setEnabled(true);
        user.setVerificationToken(null); // Eliminamos el token tras la verificación
        userRepository.save(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ApiException("Email not found"));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getRealUserName(), email, token);
    }

    public void resetPassword(String token, String password) {
        User user = userRepository.findByPasswordResetToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.isEnabled()) {
            user.setPassword(passwordEncoder.encode(password));
            user.setPasswordResetToken(null); // Eliminamos el token tras el restablecimiento
            userRepository.save(user);
        } else {
            throw new ApiException("La cuenta no está activada. Por favor, activa tu cuenta antes de restablecer la contraseña.");
        }
    }


}