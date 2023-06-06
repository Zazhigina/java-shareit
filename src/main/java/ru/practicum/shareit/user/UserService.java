package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;

    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        validEmail(user);
        validation(user);
        return UserMapper.toUserDto(userDao.add(user));
    }

    public UserDto update(Long userId, UserDto userDto) {
        if (!(userNotExists(userId))) {
            log.error("user service получает пользователя по ошибке: user с id {} не найден.", userId);
            throw new NotFoundException(String.format("Пользователь с id: %s не найден!", userId));
        }
        User user = UserMapper.toUser(userDto);
        UserDto userFromStorage = getUserById(userId);
        if (Objects.isNull(user.getName())) {
            user.setName(userFromStorage.getName());
        }
        if (Objects.isNull(user.getEmail())) {
            user.setEmail(userFromStorage.getEmail());
        } else {
            String email = user.getEmail();
            boolean isEmailNotChange = userFromStorage.getEmail().equals(email);
            if (!isEmailNotChange) {
                validEmail(user);
            }
        }
        user.setId(userId);
        validation(user);
        return UserMapper.toUserDto(userDao.update(userId, user));
    }

    public List<UserDto> getAll() {
        return userDao.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public void delete(Long userId) {
        userNotExists(userId);
        userDao.delete(userId);
    }

    public UserDto getUserById(Long userId) {
        if (!userNotExists(userId)) {
            log.debug("Пользователь с  id {} не найден.", userId);
            throw new NotFoundException(String.format("Пользователь с id: %s не найден!", userId));
        }
        User user = userDao.getUserById(userId);
        validation(user);
        return UserMapper.toUserDto(user);
    }

    private void validation(User user) throws ValidationException {
        if (!StringUtils.hasText(user.getEmail())) {
            log.warn("Неправильно ввели почту");
            throw new ValidationException("Адрес электронной почты не может быть пустым.");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Неправильно ввели почту");
            throw new ValidationException("Адрес электронной почты не содержит @.");
        }
        if (user.getName().isBlank()) {
            log.warn("Неправильно ввели имя");
            throw new ValidationException("Имя пользователя не может быть пустым.");
        }
        if (user.getName().contains(" ")) {
            log.warn("Неправильно ввели имя");
            throw new ValidationException("Имя пользователя не может быть пустым");
        }
    }

    private boolean userNotExists(Long user) {
        for (User user1 : userDao.getAll()) {
            if (Objects.equals(user, user1.getId())) {
                return true;
            }
        }
        return false;
    }

    private void validEmail(User user) {
        String email = user.getEmail();
        for (User user1 : userDao.getAll()) {
            if (user1.getEmail().contains(email)) {
                log.error("user service получает email по ошибке: email {} уже существует.", email);
                throw new ValidationException("Адрес электронной почты уже существует.");
            }
        }
    }
}


