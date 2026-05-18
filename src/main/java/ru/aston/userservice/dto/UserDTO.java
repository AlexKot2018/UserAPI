package ru.aston.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Сущность пользователя с навигационными ссылками (HATEOAS)")
public class UserDTO extends RepresentationModel<UserDTO> {

    @Schema(description = "Уникальный идентификатор пользователя", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
    @Schema(description = "Имя пользователя", example = "Иван", minLength = 2, maxLength = 50)
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Электронная почта", example = "ivan@example.com")
    private String email;

    @Min(value = 0, message = "Возраст не может быть отрицательным")
    @Max(value = 150, message = "Указан слишком большой возраст")
    @Schema(description = "Возраст пользователя", example = "25", minimum = "0", maximum = "150")
    private Integer age;

    @Schema(description = "Дата и время создания профиля", example = "2026-05-18T15:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
