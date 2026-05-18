package ru.aston.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import ru.aston.userservice.dto.UserDTO;
import ru.aston.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "Управление данными пользователей с поддержкой HATEOAS")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Получить список всех пользователей")
    public ResponseEntity<CollectionModel<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        for (UserDTO user : users) {
            user.add(linkTo(methodOn(UserController.class).getUser(user.getId())).withSelfRel());
        }

        Link selfLink = linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel();
        CollectionModel<UserDTO> result = CollectionModel.of(users, selfLink);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO userDTO = userService.getUser(id);
        if (userDTO != null) {
            userDTO.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
            userDTO.add(linkTo(methodOn(UserController.class).updateUser(id, userDTO)).withRel("update"));
            userDTO.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
            userDTO.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Создать нового пользователя")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.saveUser(userDTO);
        createdUser.add(linkTo(methodOn(UserController.class).getUser(createdUser.getId())).withSelfRel());

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные пользователя")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        if (updatedUser != null) {
            updatedUser.add(linkTo(methodOn(UserController.class).getUser(id)).withSelfRel());
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
