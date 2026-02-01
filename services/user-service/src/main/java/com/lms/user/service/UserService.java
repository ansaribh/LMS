package com.lms.user.service;

import com.lms.common.dto.PagedResponse;
import com.lms.common.dto.UserDto;
import com.lms.common.enums.UserRole;
import com.lms.common.exception.ConflictException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.user.dto.CreateUserRequest;
import com.lms.user.dto.UpdateUserRequest;
import com.lms.user.entity.User;
import com.lms.user.mapper.UserMapper;
import com.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findByActiveTrue(pageable);
        List<UserDto> userDtos = users.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(users, userDtos);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByKeycloakId(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "keycloakId", keycloakId));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User", "email", request.getEmail());
        }

        User user = User.builder()
                .keycloakId(request.getKeycloakId())
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .bio(request.getBio())
                .roles(request.getRoles() != null ? request.getRoles() : Set.of(UserRole.STUDENT))
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("User created: {}", user.getUsername());

        return userMapper.toDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("User", "email", request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        user = userRepository.save(user);
        log.info("User updated: {}", user.getUsername());

        return userMapper.toDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        // Soft delete
        user.setActive(false);
        userRepository.save(user);
        log.info("User deactivated: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> getUsersByRole(UserRole role, Pageable pageable) {
        Page<User> users = userRepository.findByRole(role, pageable);
        List<UserDto> userDtos = users.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(users, userDtos);
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> searchUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(search, pageable);
        List<UserDto> userDtos = users.getContent().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        return PagedResponse.of(users, userDtos);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto addRole(UUID userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.addRole(role);
        user = userRepository.save(user);
        log.info("Role {} added to user {}", role, user.getUsername());
        
        return userMapper.toDto(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto removeRole(UUID userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.removeRole(role);
        user = userRepository.save(user);
        log.info("Role {} removed from user {}", role, user.getUsername());
        
        return userMapper.toDto(user);
    }

    // Parent-Student relationship methods
    @Transactional
    public void linkParentToStudent(UUID parentId, UUID studentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        if (!parent.hasRole(UserRole.PARENT)) {
            throw new IllegalArgumentException("User is not a parent");
        }
        if (!student.hasRole(UserRole.STUDENT)) {
            throw new IllegalArgumentException("User is not a student");
        }

        parent.addChild(student);
        userRepository.save(parent);
        log.info("Parent {} linked to student {}", parent.getUsername(), student.getUsername());
    }

    @Transactional
    public void unlinkParentFromStudent(UUID parentId, UUID studentId) {
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", "id", parentId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId));

        parent.removeChild(student);
        userRepository.save(parent);
        log.info("Parent {} unlinked from student {}", parent.getUsername(), student.getUsername());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getChildrenByParentId(UUID parentId) {
        List<User> children = userRepository.findChildrenByParentId(parentId);
        return children.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getParentsByStudentId(UUID studentId) {
        List<User> parents = userRepository.findParentsByStudentId(studentId);
        return parents.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
