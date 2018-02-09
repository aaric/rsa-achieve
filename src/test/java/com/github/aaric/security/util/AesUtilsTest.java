package com.github.aaric.security.util;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AesUtilTest
 *
 * @author Aaric, created on 2018-02-09T11:22.
 * @since 1.0-SNAPSHOT
 */
public class AesUtilsTest {

    @Test
    public void testAes() throws Exception {
        String algorithmName = "AES";
        String content = "hello world";

        byte[] seedBytes = "random".getBytes();

        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithmName);
        keyGenerator.init(128, new SecureRandom(seedBytes));
        SecretKey secretKey = keyGenerator.generateKey();
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getEncoded(), algorithmName);

        // 加密内容
        Cipher cipher = Cipher.getInstance(algorithmName);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] secret = cipher.doFinal(content.getBytes());

        // 解密内容
        cipher = Cipher.getInstance(algorithmName);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        System.out.println(new String(cipher.doFinal(secret)));
    }

    @Test
    public void testGenerateAesSecret() throws Exception {
        byte[] secretKeyBytes = AesUtils.generateAesSecret();
        System.out.println(DatatypeConverter.printHexBinary(secretKeyBytes));
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String content = "hello world";
        byte[] secretKeyBytes = AesUtils.generateAesSecret();

        byte[] secret = AesUtils.encrypt(content.getBytes(), secretKeyBytes);
        Assert.assertEquals(content, new String(AesUtils.decrypt(secret, secretKeyBytes)));
    }

    private String encodeToBase64String(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    public void testEncryptAndDecrypt2() throws Exception {
        String content = "hello world";
        String secretKeyString = encodeToBase64String(AesUtils.generateAesSecret());

        byte[] secret = AesUtils.encrypt(content.getBytes(), secretKeyString);
        Assert.assertEquals(content, new String(AesUtils.decrypt(secret, secretKeyString)));
    }
}
