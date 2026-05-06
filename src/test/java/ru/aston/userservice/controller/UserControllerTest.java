package ru.aston.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.aston.userservice.dto.UserDTO;
import ru.aston.userservice.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO(1L, "Alex", "alex@mail.com", 25, LocalDateTime.now());
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/users"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("Alex"))
               .andExpect(jsonPath("$[0].email").value("alex@mail.com"));
    }

    @Test
    void getUser_ExistingId_ShouldReturnUser() throws Exception {
        when(userService.getUser(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Alex"));
    }

    @Test
    void getUser_NonExistingId_ShouldReturn404() throws Exception {
        when(userService.getUser(99L)).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
               .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ShouldReturn201AndUser() throws Exception {
        when(userService.saveUser(any(UserDTO.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(testUser)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.name").value("Alex"));
    }

    @Test
    void deleteUser_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
               .andExpect(status().isNoContent());
    }
}
