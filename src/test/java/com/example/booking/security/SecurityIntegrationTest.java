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
        userRepository.deleteAll();

        // Create standard user
        User user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);
        userToken = "Bearer " + tokenProvider.generateToken(user.getEmail(), user.getRole().name());

        // Create admin user
        User admin = new User();
        admin.setEmail("testadmin@example.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
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
