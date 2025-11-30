package com.springboot.FlomadAIplanner.security;

import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class AESEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    public static void main(String[] args) throws Exception {
        String filePath = ".env";
        byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

        SecretKey secretKey = generateSecretKey();
        byte[] iv = generateIV();
        byte[] encryptedData = encrypt(fileContent, secretKey, iv);

        // Encryption completed successfully
        System.out.println("File encrypted successfully. Encrypted data size: " + encryptedData.length + " bytes");
        // You can store the secretKey and iv securely if needed
    }

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(AES_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    public static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(byte[] data, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] encryptedData, SecretKey key, byte[] iv) throws InvalidAlgorithmParameterException,
            InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher1 = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec1 = new GCMParameterSpec(GCM_IV_LENGTH * 8, iv);
        cipher1.init(Cipher.DECRYPT_MODE, key, spec1);
        return cipher1.doFinal(encryptedData);
    }
}

