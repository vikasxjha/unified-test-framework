package com.company.qa.unified.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * EncryptionUtils
 *
 * Secure utilities for:
 * - AES-GCM encryption/decryption (preferred)
 * - Password hashing (PBKDF2)
 * - HMAC signing
 * - SHA-256 hashing
 * - Base64 helpers
 *
 * Notes:
 * - Uses Java 17 standard crypto only
 * - CI-safe
 * - Deterministic options available for testing
 */
public final class EncryptionUtils {

    private static final Log log =
            Log.get(EncryptionUtils.class);

    private static final SecureRandom RANDOM =
            new SecureRandom();

    /* AES-GCM constants */
    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12; // recommended

    /* PBKDF2 constants */
    private static final String PBKDF2 =
            "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 65_536;
    private static final int PBKDF2_KEY_LENGTH_BITS = 256;

    private EncryptionUtils() {
        // utility class
    }

    /* =========================================================
       AES-GCM ENCRYPTION / DECRYPTION
       ========================================================= */

    /**
     * Generate a random AES key (256-bit).
     */
    public static SecretKey generateAesKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES);
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate AES key", e);
        }
    }

    /**
     * Encrypt plaintext using AES-GCM.
     * Output format (Base64):
     * [12 bytes IV][ciphertext+tag]
     */
    public static String encryptAesGcm(
            String plaintext,
            SecretKey key
    ) {

        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec =
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText =
                    cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer =
                    ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return Base64.getEncoder()
                    .encodeToString(buffer.array());

        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM encryption failed", e);
        }
    }

    /**
     * Decrypt AES-GCM payload produced by encryptAesGcm.
     */
    public static String decryptAesGcm(
            String encryptedBase64,
            SecretKey key
    ) {

        try {
            byte[] decoded =
                    Base64.getDecoder().decode(encryptedBase64);

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            buffer.get(iv);

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec =
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plain =
                    cipher.doFinal(cipherText);

            return new String(plain, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("AES-GCM decryption failed", e);
        }
    }

    /* =========================================================
       PASSWORD HASHING (PBKDF2)
       ========================================================= */

    /**
     * Hash password using PBKDF2 (safe for credentials in tests).
     */
    public static String hashPassword(
            char[] password,
            byte[] salt
    ) {

        try {
            PBEKeySpec spec =
                    new PBEKeySpec(
                            password,
                            salt,
                            PBKDF2_ITERATIONS,
                            PBKDF2_KEY_LENGTH_BITS
                    );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(PBKDF2);

            byte[] hash =
                    factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    /**
     * Generate secure random salt.
     */
    public static byte[] generateSalt(int bytes) {
        byte[] salt = new byte[bytes];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /* =========================================================
       HMAC SIGNING
       ========================================================= */

    public static String hmacSha256(
            String data,
            String secret
    ) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key =
                    new SecretKeySpec(
                            secret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            mac.init(key);

            byte[] raw =
                    mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(raw);

        } catch (Exception e) {
            throw new IllegalStateException("HMAC calculation failed", e);
        }
    }

    /* =========================================================
       HASHING (SHA-256)
       ========================================================= */

    public static String sha256(String input) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            input.getBytes(StandardCharsets.UTF_8)
                    );

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 hashing failed", e);
        }
    }

    /* =========================================================
       BASE64 HELPERS
       ========================================================= */

    public static String base64Encode(String input) {
        return Base64.getEncoder()
                .encodeToString(
                        input.getBytes(StandardCharsets.UTF_8)
                );
    }

    public static String base64Decode(String base64) {
        return new String(
                Base64.getDecoder().decode(base64),
                StandardCharsets.UTF_8
        );
    }
}
