package com.springboot.FlomadAIplanner.service;

import com.springboot.FlomadAIplanner.security.AESEncryption;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class EncryptionService {

    private final AESEncryption aesEncryption;

    public EncryptionService(AESEncryption aesEncryption) {
        this.aesEncryption = aesEncryption;
    }

    public String encryptSensitiveData(String data) throws Exception {
        SecretKey key = AESEncryption.generateSecretKey();
        byte[] iv = AESEncryption.generateIV();
        byte[] encryptedData = AESEncryption.encrypt(data.getBytes(), key, iv);

        // Store key and iv securely, or encode them with the data for easy retrieval
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        String encodedIV = Base64.getEncoder().encodeToString(iv);
        String encodedData = Base64.getEncoder().encodeToString(encryptedData);

        // Return the concatenated result, or store the key/IV separately
        return encodedIV + ":" + encodedKey + ":" + encodedData;
    }

    public String decryptSensitiveData(String encryptedDataWithIV) throws InvalidAlgorithmParameterException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Split the encrypted data into its components (IV, key, data)
        String[] parts = encryptedDataWithIV.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] keyBytes = Base64.getDecoder().decode(parts[1]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[2]);

        // Recreate the key from the stored key bytes
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        // Decrypt the data
        byte[] decryptedData = aesEncryption.decrypt(encryptedData, key, iv);
        return new String(decryptedData);
    }
}

