package es.uca.gamebox;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.exception.ApiException;
import es.uca.gamebox.repository.UserRepository;
import es.uca.gamebox.service.EmailService;
import es.uca.gamebox.service.FakeEmailService;
import es.uca.gamebox.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import({UserService.class, UserServiceTest.TestConfig.class})
@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        public EmailService emailService() {
            return new FakeEmailService();
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        testUser.setIsAdmin(false);
        userRepository.save(testUser);
    }

    @Test
    void shouldCreateUser_whenDataIsValid() {
        MultipartFile avatar = new MockMultipartFile("avatar", new byte[0]);

        userService.createUser("newuser", "pass123", "new@example.com", avatar);

        Optional<User> created = userRepository.findByEmail("new@example.com");
        assertTrue(created.isPresent());
        assertTrue(passwordEncoder.matches("pass123", created.get().getPassword()));
    }

    @Test
    void shouldThrowException_whenEmailAlreadyExists() {
        MultipartFile avatar = new MockMultipartFile("avatar", new byte[0]);

        ApiException ex = assertThrows(ApiException.class, () -> {
            userService.createUser("otheruser", "pass123", "test@example.com", avatar);
        });

        assertEquals("El email ya está registrado", ex.getMessage());
    }

    @Test
    void shouldUpdateUser_whenFieldsAreValid() {
        MultipartFile avatar = new MockMultipartFile("avatar", new byte[0]);

        userService.updateUser(testUser, "updatedName", "newpass", "updated@example.com", avatar);
        User updated = userRepository.findById(testUser.getId()).orElseThrow();

        assertEquals("updatedName", updated.getRealUserName());
        assertEquals("updated@example.com", updated.getEmail());
        assertTrue(passwordEncoder.matches("newpass", updated.getPassword()));
    }

    @Test
    void shouldVerifyAccount_whenTokenIsValid() {
        testUser.setEnabled(false);
        testUser.setVerificationToken("valid-token");
        userRepository.save(testUser);

        userService.verifyAccount("valid-token");

        User verified = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(verified.isEnabled());
        assertNull(verified.getVerificationToken());
    }

    @Test
    void shouldThrowException_whenVerifyTokenIsInvalid() {
        assertThrows(RuntimeException.class, () -> {
            userService.verifyAccount("invalid-token");
        });
    }

    @Test
    void shouldResetPassword_whenTokenIsValidAndUserIsEnabled() {
        testUser.setPasswordResetToken("reset-token");
        testUser.setEnabled(true);
        userRepository.save(testUser);

        userService.resetPassword("reset-token", "newpass");

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newpass", updated.getPassword()));
        assertNull(updated.getPasswordResetToken());
    }

    @Test
    void shouldThrowException_whenResettingPasswordForInactiveUser() {
        testUser.setPasswordResetToken("reset-token");
        testUser.setEnabled(false);
        userRepository.save(testUser);

        ApiException ex = assertThrows(ApiException.class, () -> {
            userService.resetPassword("reset-token", "newpass");
        });

        assertTrue(ex.getMessage().contains("La cuenta no está activada"));
    }

    @Test
    void shouldGenerateFriendCode_whenMissing() {
        testUser.setFriendCode(null);
        userRepository.save(testUser);

        String code = userService.getOrGenerateFriendCode(testUser.getId());
        assertNotNull(code);
        assertEquals(code, userRepository.findById(testUser.getId()).get().getFriendCode());
    }

    @Test
    void shouldNotChangeFriendCode_whenAlreadyExists() {
        testUser.setFriendCode("1234567890");
        userRepository.save(testUser);

        String code = userService.getOrGenerateFriendCode(testUser.getId());
        assertEquals("1234567890", code);
    }

    @Test
    void shouldAddFriendBidirectionally() {
        User friend = new User();
        friend.setUsername("amigo");
        friend.setEmail("friend@example.com");
        friend.setPassword(passwordEncoder.encode("pass"));
        friend = userRepository.save(friend);

        userService.addFriend(testUser.getId(), friend.getId());

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        User updatedFriend = userRepository.findById(friend.getId()).orElseThrow();

        assertTrue(updatedUser.getFriends().contains(friend));
        assertTrue(updatedFriend.getFriends().contains(testUser));
    }

    @Test
    void shouldThrowException_whenAddingSelfAsFriend() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.addFriend(testUser.getId(), testUser.getId());
        });
    }
}
