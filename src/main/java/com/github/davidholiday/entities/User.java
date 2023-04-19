package com.github.davidholiday.entities;

import com.github.davidholiday.Pair;
import com.github.davidholiday.Role;
import jakarta.persistence.*;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.io.Serializable;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import java.util.Base64;


@Entity
public class User implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    //

    private static final String ALPHANUMERIC_PATTERN = "^[a-zA-Z0-9]+$";

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAX_PASSWORD_LENGTH = 64;

    private static final int MIN_USERNAME_LENGTH = 4;

    private static final int MAX_USERNAME_LENGTH = 16;

    //

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column(length=MAX_USERNAME_LENGTH, nullable=false, unique=true)
    private String username;

    @Column(length=256, nullable=false)
    private String password;

    @Column(length=256, nullable=false)
    private String salt;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    //

    public Long getId() { return id; }

    //

    public String getUsername() { return username; }

    public void setUsername(@NotNull String username) {
        if (username.matches(ALPHANUMERIC_PATTERN) == false) {
            LOG.error("refusing to set username to: {} because it has more than letters and numbers", username);
            throw new IllegalArgumentException();
        } else if (username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH) {
            LOG.error(
                    "refusing to set username to: {} because it does not meet size constraint between {} and {} chars",
                    username,
                    MIN_USERNAME_LENGTH,
                    MAX_PASSWORD_LENGTH
            );
            throw new IllegalArgumentException();
        }

        this.username = username;
    }

    //

    public String getPassword() { return password; }

    public void setPassword(@NotNull String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            LOG.error(
                    "refusing to set password to: {} because it does not meet size constraint between {} and {} chars",
                    password,
                    MIN_PASSWORD_LENGTH,
                    MAX_PASSWORD_LENGTH
            );
            throw new IllegalArgumentException();
        }

        var passwordPair = getHashAndSaltedPassword(password);
        var hashedAndSaltedPassword = passwordPair.getLeft();
        var salt = passwordPair.getRight();

        this.password = hashedAndSaltedPassword;
        setSalt(salt);
    }

    public boolean checkUnhashedEquality(@NotNull String unhashedPassword)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        Pair<String, String> hashAndSalt = getHashAndSaltedPassword(unhashedPassword, getSalt());
        String hashedPassword = hashAndSalt.getLeft();
        return getPassword().equals(hashedPassword);
    }

    private Pair<String, String> getHashAndSaltedPassword(String password, String... salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // ty internets
        // https://pages.nist.gov/800-63-3/sp800-63b.html
        // https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
        // https://www.baeldung.com/java-password-hashing
        // https://security.stackexchange.com/questions/25510/how-to-authenticate-a-salted-password
        // https://stackoverflow.com/a/19349167/2234770

        byte[] saltBytes;
        if (salt.length > 0) {
            Base64.Decoder base64Decoder = Base64.getDecoder();
            saltBytes = base64Decoder.decode(salt[0]);
        } else {
            saltBytes = getSaltBytes();
        }

        KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 512);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] hashedAndSaltedPasswordBytes = factory.generateSecret(spec).getEncoded();

        Base64.Encoder base64Encoder = Base64.getEncoder();
        String hashedAndSaltedPassword = base64Encoder.encodeToString(hashedAndSaltedPasswordBytes);
        String saltOut = base64Encoder.encodeToString(saltBytes);

        return new Pair<>(hashedAndSaltedPassword, saltOut);
    }

    private byte[] getSaltBytes() {
        SecureRandom randy = new SecureRandom();
        byte[] saltBytes = new byte[64];
        randy.nextBytes(saltBytes);
        return saltBytes;
    }

    //

    public String getSalt() { return salt; }

    // salt is set by setPassword - never directly by any external caller
    private void setSalt(@NotNull String salt) { this.salt = salt; }

    //

    public Role getRole() { return role; }

    public void setRole(@NotNull Role role) { this.role = role; }
}
