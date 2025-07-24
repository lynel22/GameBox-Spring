package es.uca.gamebox.service;

import es.uca.gamebox.dto.FriendDto;
import es.uca.gamebox.dto.GameSummaryDto;
import es.uca.gamebox.dto.UserProfileDto;
import es.uca.gamebox.entity.*;
import es.uca.gamebox.exception.ApiException;
import es.uca.gamebox.repository.AchievementRepository;
import es.uca.gamebox.repository.AchievementUserRepository;
import es.uca.gamebox.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private final AchievementUserRepository achievementUserRepository;
    @Autowired
    private final AchievementRepository achievementRepository;

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
            storeImg(user, avatar);
        }

        userRepository.save(user);
        emailService.sendNewAccountEmail(username, email, token);
        log.info("User created: {}", username);
    }

    public void updateUser(User user, String username, String password, String email, MultipartFile avatar) {
        if (username != null) user.setUsername(username);
        if (password != null) user.setPassword(passwordEncoder.encode(password));
        if (email != null) user.setEmail(email);

        if (avatar != null && !avatar.isEmpty()) {
            if (user.getImageUrl() != null) {
                try {
                    Path oldAvatarPath = Paths.get("uploads", "avatars", Paths.get(user.getImageUrl()).getFileName().toString());
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
        Path uploadPath = Paths.get("uploads", "avatars");

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(avatar.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            user.setImageUrl("/uploads/avatars/" + fileName); // Ruta accesible públicamente
        } catch (Exception e) {
            throw new ApiException("Error al guardar el avatar: " + e.getMessage());
        }
    }

    public void verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));
        System.out.println("Verifying user: " + user.getUsername());
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

    public void saveSteamId(UUID userId, String steamId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        user.setSteamId(steamId);
        System.out.println("Saving Steam ID: " + steamId + " for user: " + user.getUsername());
        userRepository.save(user);
    }


    public void unlinkSteamAccount(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("User not found"));
        user.setSteamId(null);
        userRepository.save(user);
        log.info("Steam account unlinked for user: {}", user.getUsername());
    }

    @Transactional
    public UserProfileDto getUserProfile(UUID userId) {
        // Obtener usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Obtener todos los GameUser del usuario desde sus bibliotecas
        List<GameUser> gameUsers = user.getLibraries().stream()
                .flatMap(library -> {
                    List<GameUser> gus = library.getGameUsers();
                    return gus != null ? gus.stream() : Stream.empty();
                })
                .toList();

        List<GameSummaryDto> gameSummaries = new ArrayList<>();

        for (GameUser gameUser : gameUsers) {
            Game game = gameUser.getGame();
            Store store = gameUser.getLibrary().getStore();

            int achievementsUnlocked = achievementUserRepository.countByGameUserId(gameUser.getId());
            int totalAchievements = game.getAchievements() != null ? game.getAchievements().size() : 0;

            GameSummaryDto dto = new GameSummaryDto();
            dto.setId(game.getId());
            dto.setName(game.getName());
            dto.setImageUrl(game.getImageUrl());
            dto.setStoreName(store != null ? store.getName() : null);
            dto.setStoreImageUrl(store != null ? store.getImageUrl() : null);
            dto.setHoursPlayed(gameUser.getHoursPlayed());
            dto.setLastSession(gameUser.getLastPlayed());
            dto.setAchievementsUnlocked(achievementsUnlocked);
            dto.setTotalAchievements(totalAchievements);

            gameSummaries.add(dto);
        }

        // 🛠️ Construcción manual del UserProfileDto
        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setId(user.getId());
        profileDto.setUsername(user.getRealUserName());
        profileDto.setEmail(user.getEmail());
        profileDto.setImageUrl(user.getImageUrl());
        profileDto.setSteamId(user.getSteamId());

        // Asignar amigos (puedes mapear a un DTO si prefieres)
        profileDto.setFriends(
                user.getFriends().stream()
                        .map(friend -> {
                            FriendDto friendDto = new FriendDto();
                            friendDto.setId(friend.getId());
                            friendDto.setUsername(friend.getUsername());
                            friendDto.setImageUrl(friend.getImageUrl());
                            return friendDto;
                        })
                        .collect(Collectors.toList())
        );

        profileDto.setGames(gameSummaries);
        profileDto.setTotalGames(gameSummaries.size());

        return profileDto;
    }


    public String getOrGenerateFriendCode(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getFriendCode() == null) {
            String generatedCode;
            do {
                generatedCode = String.valueOf(ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L));
            } while (userRepository.existsByFriendCode(generatedCode));

            user.setFriendCode(generatedCode);
            userRepository.save(user);
        }

        return user.getFriendCode();
    }


    public Optional<FriendDto> findFriendByCode(String code) {
        return userRepository.findByFriendCode(code)
                .map(user -> {
                    FriendDto dto = new FriendDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setImageUrl(user.getImageUrl());
                    return dto;
                });
    }

    @Transactional
    public void addFriend(UUID userId, UUID friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("No puedes agregarte a ti mismo como amigo");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new EntityNotFoundException("Amigo no encontrado"));

        // Añadir bidireccionalmente si aún no lo son
        if (!user.getFriends().contains(friend)) {
            user.getFriends().add(friend);
        }
        if (!friend.getFriends().contains(user)) {
            friend.getFriends().add(user);
        }

        userRepository.save(user);
        userRepository.save(friend);
    }

    public List<FriendDto> getFriendsOfUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        return user.getFriends().stream().map(friend -> {
            FriendDto dto = new FriendDto();
            dto.setId(friend.getId());
            dto.setUsername(friend.getRealUserName());
            dto.setImageUrl(friend.getImageUrl());
            return dto;
        }).toList();
    }

}