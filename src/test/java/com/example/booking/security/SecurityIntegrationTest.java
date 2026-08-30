package com.example.booking.security;

import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String userToken;
    private String adminToken;

    @BeforeEach
    public void setUp() {
        // Find or create test user with unique email to avoid seeding conflicts
        User user = userRepository.findByEmail("testuser@example.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail("testuser@example.com");
                    u.setPassword(passwordEncoder.encode("password123"));
                    u.setRole(Role.USER);
                    return userRepository.save(u);
                });
        userToken = "Bearer " + tokenProvider.generateToken(user.getEmail(), user.getRole().name());

        // Find or create admin user with unique email to avoid seeding conflicts
        User admin = userRepository.findByEmail("testadmin@example.com")
                .orElseGet(() -> {
                    User a = new User();
                    a.setEmail("testadmin@example.com");
                    a.setPassword(passwordEncoder.encode("password123"));
                    a.setRole(Role.ADMIN);
                    return userRepository.save(a);
                });
        adminToken = "Bearer " + tokenProvider.generateToken(admin.getEmail(), admin.getRole().name());
    }

    @Test
    public void unauthorizedAccess_Blocked() throws Exception {
        mockMvc.perform(get("/resources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getResources_AllowedForUserAndAdmin() throws Exception {
        mockMvc.perform(get("/resources")
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/resources")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void createResource_AllowedForAdminOnly() throws Exception {
        String resourceRequestJson = "{\"name\":\"Room X\",\"description\":\"desc\"}";

        // User should be forbidden (403)
        mockMvc.perform(post("/resources")
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceRequestJson))
                .andExpect(status().isForbidden());

        // Admin should be allowed (201)
        mockMvc.perform(post("/resources")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(resourceRequestJson))
                .andExpect(status().isCreated());
    }
}
