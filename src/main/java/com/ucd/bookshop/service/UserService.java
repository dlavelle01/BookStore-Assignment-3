package com.ucd.bookshop.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

        // Adding for 2FA
        /*
        user.setIsUsing2FA(user.getIsUsing2FA());
        user.setSecret(user.getSecret());

         */

        user.setIsUsing2FA(false);
        user.setSecret("");

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
            return new UserDto(user.getUserId(), user.getUserName(), user.getRoleId(), user.getSecret(), user.getIsUsing2FA());
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

    //public UserDto updateUser2FA(boolean use2FA);

    public String generateQRUrl(UserDto user) throws UnsupportedEncodingException {
        String issuer = "BookShop";
        String label  = user.getUserName();

        // fetch the secret from the database for this username
        User dbUser = userRepository.findByUserName(label);
        if (dbUser == null || dbUser.getSecret() == null || dbUser.getSecret().isBlank()) {
            throw new IllegalStateException("No 2FA secret for user: " + label);
        }
        String secret = dbUser.getSecret();

        String otpauth = "otpauth://totp/"
                + URLEncoder.encode(issuer + ":" + label, StandardCharsets.UTF_8)
                + "?secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                + "&issuer=" + URLEncoder.encode(issuer, StandardCharsets.UTF_8)
                + "&digits=6&period=30&algorithm=SHA1";

        return "https://quickchart.io/qr?size=200&text=" + URLEncoder.encode(otpauth, StandardCharsets.UTF_8);

    }

    /*
    public UserDto updateUser2FA(boolean use2FA);

    @Override
    public UserDto updateUser2FA(boolean use2FA) {
        Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) curAuth.getPrincipal();

        currentUser.setUsing2FA(use2FA);
        currentUser.setSecret(Base32.random());

        currentUser = userRepository.save(currentUser);
        UserDto userDto = convertEntityToDto(currentUser);
        return userDto;
    }

     */
    private String generateNewSecret() {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"; // RFC 4648 Base32
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private UserDto convertEntityToDto(User u) {
        return new UserDto(u.getUserId(), u.getUserName(), u.getRoleId(), u.getSecret(), u.getIsUsing2FA());
    }

    public UserDto updateUser2FA(boolean use2FA) {
        Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) curAuth.getPrincipal();

        currentUser.setIsUsing2FA(use2FA);
        if (use2FA) {
            // only set a new secret if none exists (so users don't lose their existing app setup)
            if (currentUser.getSecret() == null || currentUser.getSecret().isBlank()) {
                currentUser.setSecret(generateNewSecret());
            }
        } else {
            // optional: keep secret for potential re-enable, or wipe it:
            // currentUser.setSecret("");
        }

        currentUser = userRepository.save(currentUser);
        return convertEntityToDto(currentUser);
    }


}