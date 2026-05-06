package ru.aston.userservice.service;

import org.springframework.transaction.annotation.Transactional;
import ru.aston.userservice.dto.UserDTO;
import ru.aston.userservice.model.User;
import ru.aston.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDTO saveUser(UserDTO userDTO) {
        log.info("Попытка сохранения нового пользователя с email: {}", userDTO.getEmail());
        User user = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getAge());
        User savedUser = userRepository.save(user);
        log.info("Пользователь сохранен успешно. ID: {}", savedUser.getId());
        return convertToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUser(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userRepository.findById(id)
                             .map(this::convertToDTO)
                             .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userRepository.findAll().stream()
                             .map(this::convertToDTO)
                             .collect(Collectors.toList());
    }

    @Override
    @Transactional // Важно: если поиск успешен, save выполнится в той же транзакции
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Обновление данных пользователя с ID: {}", id);
        return userRepository.findById(id).map(user -> {
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setAge(userDTO.getAge());
            User updated = userRepository.save(user);
            log.info("Данные пользователя с ID {} успешно обновлены", id);
            return convertToDTO(updated);
        }).orElse(null);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.info("Пользователь с ID {} удален", id);
        }
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}