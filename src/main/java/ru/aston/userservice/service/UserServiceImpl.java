package ru.aston.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.userservice.dto.UserDTO;
import ru.aston.userservice.exception.EmailAlreadyExistsException;
import ru.aston.userservice.exception.ResourceNotFoundException;
import ru.aston.userservice.model.User;
import ru.aston.userservice.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDTO saveUser(UserDTO userDTO) {
        log.info("Запрос на создание пользователя с email: {}", userDTO.getEmail());

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Пользователь с таким email уже зарегистрирован");
        }

        User user = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getAge());
        User savedUser = userRepository.save(user);

        log.info("Пользователь создан с ID: {}", savedUser.getId());
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO getUser(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);
        return userRepository.findById(id)
                             .map(this::convertToDTO)
                             .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userRepository.findAll().stream()
                             .map(this::convertToDTO)
                             .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Запрос на обновление пользователя с ID: {}", id);

        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new ResourceNotFoundException("Невозможно обновить: пользователь с ID " + id + " не найден"));

        if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email " + userDTO.getEmail() + " уже занят");
        }

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        User updated = userRepository.save(user);
        log.info("Данные пользователя с ID {} успешно обновлены", id);
        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Невозможно удалить: пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
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
