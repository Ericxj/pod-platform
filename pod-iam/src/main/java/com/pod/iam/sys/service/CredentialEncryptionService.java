package com.pod.iam.sys.service;

import com.pod.common.core.exception.BusinessException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 凭证载荷加密/解密。密钥来自配置 pod.sys.credential.secret（至少 16 字节）。
 */
public class CredentialEncryptionService {

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String ALG = "AES/GCM/NoPadding";

    private final byte[] keyBytes;

    public CredentialEncryptionService(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 16) {
            throw new IllegalArgumentException("credential secret must be at least 16 bytes");
        }
        this.keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (this.keyBytes.length > 32) {
            byte[] k = new byte[32];
            System.arraycopy(this.keyBytes, 0, k, 0, 32);
            // use first 32 as key for AES-256; else we'd need to hash
        }
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            SecureRandom r = new SecureRandom();
            byte[] iv = new byte[GCM_IV_LENGTH];
            r.nextBytes(iv);
            SecretKeySpec key = new SecretKeySpec(ensureKeyLength(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] enc = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(enc, 0, out, iv.length, enc.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new BusinessException("Credential encrypt failed: " + e.getMessage());
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) return null;
        try {
            byte[] raw = Base64.getDecoder().decode(encrypted);
            if (raw.length < GCM_IV_LENGTH) throw new BusinessException("Invalid encrypted payload");
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(raw, 0, iv, 0, GCM_IV_LENGTH);
            byte[] enc = new byte[raw.length - GCM_IV_LENGTH];
            System.arraycopy(raw, GCM_IV_LENGTH, enc, 0, enc.length);
            SecretKeySpec key = new SecretKeySpec(ensureKeyLength(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher c = Cipher.getInstance(ALG);
            c.init(Cipher.DECRYPT_MODE, key, spec);
            return new String(c.doFinal(enc), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("Credential decrypt failed: " + e.getMessage());
        }
    }

    private byte[] ensureKeyLength() {
        if (keyBytes.length == 16) return keyBytes;
        if (keyBytes.length == 32) return keyBytes;
        if (keyBytes.length > 32) {
            byte[] k = new byte[32];
            System.arraycopy(keyBytes, 0, k, 0, 32);
            return k;
        }
        if (keyBytes.length == 24) return keyBytes;
        byte[] k = new byte[16];
        System.arraycopy(keyBytes, 0, k, 0, Math.min(keyBytes.length, 16));
        return k;
    }
}
