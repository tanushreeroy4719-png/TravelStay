package com.travelstay.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Password hashing based on PBKDF2WithHmacSHA256. Uses only the JDK
 * (javax.crypto / java.security), so no extra Maven dependency is required.
 *
 * Stored format: "PBKDF2:<iterations>:<base64 salt>:<base64 hash>"
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private PasswordUtil() {}

    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        return "PBKDF2:" + ITERATIONS + ":" +
                Base64.getEncoder().encodeToString(salt) + ":" +
                Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) return false;
        String[] parts = stored.split(":");
        if (parts.length != 4 || !parts[0].equals("PBKDF2")) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
            return constantTimeEquals(actual, expected);
        } catch (Exception e) {
            // Malformed stored hash or bad input – treat as "does not match".
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to hash password.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
