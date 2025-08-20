package com.ucd.bookshop.service;

import com.ucd.bookshop.constants.Role;
import com.ucd.bookshop.controllers.dto.UserDto;
import com.ucd.bookshop.controllers.dto.UserRegistrationRequestDto;
import com.ucd.bookshop.model.User;
import com.ucd.bookshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserRegistrationRequestDto request, Role role) throws NoSuchAlgorithmException {
        User user = new User();
        user.setUserName(request.getUserName());

        // Generate salt and hash password (same as mySQL)
        String salt = generateSalt();
        String hashedPassword = hashPassword(request.getPassword(), salt);

        user.setPassword(hashedPassword);
        user.setSalt(salt);
        user.setRoleId(role.getId());

        return userRepository.save(user);
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    private String generateSalt() {
        return "TS" + System.currentTimeMillis() % 100000;
    }

    /**
     * Hash password with salt using SHA-256
     * 
     * @throws NoSuchAlgorithmException
     */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String saltedPassword = salt + password;
        byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

        // Convert byte array to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }

            hexString.append(hex);
        }

        return hexString.toString();

    }

    public UserDto authenticateUser(String username, String password) throws NoSuchAlgorithmException {
        User user = userRepository.findByUserName(username);
       
        if(user != null && verifyPassword(password, user.getSalt(), user.getPassword())){
            return new UserDto(user.getUserId(), user.getUserName(), user.getRoleId());
        }

        return null;
    }

    /**
     * Verify password against stored hash
     * 
     * @throws NoSuchAlgorithmException
     */
    public boolean verifyPassword(String password, String salt, String storedHash) throws NoSuchAlgorithmException {
        String hashedPassword = hashPassword(password, salt);
        return hashedPassword.equals(storedHash);
    }
}