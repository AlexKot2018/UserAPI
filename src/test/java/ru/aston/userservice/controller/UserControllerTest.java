package ru.aston.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.aston.userservice.dto.UserDTO;
import ru.aston.userservice.exception.ResourceNotFoundException;
import ru.aston.userservice.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Тестирование контроллера пользователей")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 5, 20, 12, 0);
    private final UserDTO validUser = new UserDTO(1L, "Alex", "alex@mail.com", 25, FIXED_TIME);

    @Test
    @DisplayName("GET /api/users - успех: получение списка")
    void getAllUsers_Success() throws Exception {
        given(userService.getAllUsers()).willReturn(List.of(validUser));

        mockMvc.perform(get("/api/users"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.length()").value(1))
               .andExpect(jsonPath("$[0].email").value("alex@mail.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - успех: пользователь найден")
    void getUser_Found() throws Exception {
        given(userService.getUser(1L)).willReturn(validUser);

        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Alex"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - ошибка: пользователь не найден (404)")
    void getUser_NotFound() throws Exception {
        given(userService.getUser(99L)).willThrow(new ResourceNotFoundException("User 99 not found"));

        mockMvc.perform(get("/api/users/99"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/users - успех: валидные данные")
    void createUser_Success() throws Exception {
        given(userService.saveUser(any(UserDTO.class))).willReturn(validUser);

        mockMvc.perform(post("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(validUser)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/users - ошибка: невалидный email (400)")
    void createUser_ValidationError() throws Exception {
        UserDTO invalidUser = new UserDTO(null, "A", "not-an-email", -1, null);

        mockMvc.perform(post("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(invalidUser)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.email").exists())
               .andExpect(jsonPath("$.age").exists());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - успех: обновление данных")
    void updateUser_Success() throws Exception {
        given(userService.updateUser(eq(1L), any(UserDTO.class))).willReturn(validUser);

        mockMvc.perform(put("/api/users/1")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(validUser)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("Alex"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - успех: удаление")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
               .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
