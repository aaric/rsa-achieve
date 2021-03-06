package com.github.aaric.security.util;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * RsaUtilssTest
 *
 * @author Aaric, created on 2018-02-08T16:17.
 * @since 1.0-SNAPSHOT
 */
public class RsaUtilsTest {

    @Test
    public void testRsa() throws Exception {
        String algorithmName = "RSA";

        // 生成密钥
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithmName);
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        byte[] privateKeyBytes = privateKey.getEncoded();
        byte[] publicKeyBytes = publicKey.getEncoded();
        System.out.println("第一种方式(字节流): ");
        System.out.println("私钥(要求使用PKCS8生成加解密器): " + DatatypeConverter.printHexBinary(privateKeyBytes));
        System.out.println("公钥(要求使用X509 生成加解密器): " + DatatypeConverter.printHexBinary(publicKeyBytes));
        System.out.println("");

        String privateKeyModulus = privateKey.getModulus().toString();
        String privateKeyExponent = privateKey.getPrivateExponent().toString();
        System.out.println("第二种方式(模n和指数e)：");
        System.out.println("私钥模数: " + privateKeyModulus);
        System.out.println("私钥指数: " + privateKeyExponent);
        byte[] publicKeyModulusBytes = publicKey.getModulus().toByteArray();
        byte[] tempPublicKeyModulusBytes = new byte[publicKeyModulusBytes.length - 1];
        System.arraycopy(publicKeyModulusBytes, 1, tempPublicKeyModulusBytes, 0, tempPublicKeyModulusBytes.length);
        long publicKeyExponent = publicKey.getPublicExponent().longValue();
        System.out.println("公钥模数: " + DatatypeConverter.printHexBinary(publicKeyModulusBytes));
        System.out.println(tempPublicKeyModulusBytes.length);
        System.out.println("公钥指数: " + publicKeyExponent);


        // 加解密测试
        String data = "hello world";

        /*PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);*/
        BigInteger privateModulus = new BigInteger(privateKeyModulus);
        BigInteger privateExponent = new BigInteger(privateKeyExponent);
        RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(privateModulus, privateExponent);
        PrivateKey newPrivateKey = KeyFactory.getInstance(algorithmName).generatePrivate(privateKeySpec);
        Cipher encrypt = Cipher.getInstance(algorithmName);
        encrypt.init(Cipher.ENCRYPT_MODE, newPrivateKey);
        byte[] secret = encrypt.doFinal(data.getBytes());

        /*X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);*/
        BigInteger publicModulus = new BigInteger(1, tempPublicKeyModulusBytes);
        BigInteger publicExponent = new BigInteger(String.valueOf(publicKeyExponent));
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(publicModulus, publicExponent);
        PublicKey newPublicKey = KeyFactory.getInstance(algorithmName).generatePublic(publicKeySpec);
        Cipher decrypt = Cipher.getInstance(algorithmName);
        decrypt.init(Cipher.DECRYPT_MODE, newPublicKey);
        byte[] content = decrypt.doFinal(secret);


        Assert.assertEquals(data, new String(content));
    }

    @Test
    public void testGenerateRsaEntity() throws Exception {
        RsaUtils.RsaEntity rsaEntity = RsaUtils.generateRsaEntity();
        System.out.println(DatatypeConverter.printHexBinary(rsaEntity.getPrivateKeyBytes()));
        System.out.println(DatatypeConverter.printHexBinary(rsaEntity.getPublicKeyBytes()));
        System.out.println(DatatypeConverter.printHexBinary(rsaEntity.getPrivateKeyModulusBytes()));
        System.out.println(DatatypeConverter.printHexBinary(rsaEntity.getPrivateKeyExponentBytes()));
        System.out.println(DatatypeConverter.printHexBinary(rsaEntity.getPublicKeyModulusBytes()));
        System.out.println(rsaEntity.getPublicKeyExponent());
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String content = "hello world";
        byte[] data = content.getBytes();
        byte[] secret;

        RsaUtils.RsaEntity rsaEntity = RsaUtils.generateRsaEntity();

        // 1.测试私钥加密，公钥解密(PKCS8, X509)
        secret = RsaUtils.encryptByRsaPrivate(data, rsaEntity.getPrivateKeyBytes());
        System.out.println("1-" + new String(RsaUtils.decryptByRsaPublic(secret, rsaEntity.getPublicKeyBytes())));

        // 2.测试公钥加密，私钥解密(PKCS8, X509)
        secret = RsaUtils.encryptByRsaPublic(data, rsaEntity.getPublicKeyBytes());
        System.out.println("2-" + new String(RsaUtils.decryptByRsaPrivate(secret, rsaEntity.getPrivateKeyBytes())));

        // 3.测试私钥加密，公钥解密({e,n})
        secret = RsaUtils.encryptByRsaPrivate(data, rsaEntity.getPrivateKeyModulusBytes(), rsaEntity.getPrivateKeyExponentBytes());
        System.out.println("3-" + new String(RsaUtils.decryptByRsaPublic(secret, rsaEntity.getPrivateKeyModulusBytes(), rsaEntity.getPublicKeyExponent())));

        // 4.测试公钥加密，私钥解密({e,n})
        secret = RsaUtils.encryptByRsaPublic(data, rsaEntity.getPublicKeyModulusBytes(), rsaEntity.getPublicKeyExponent());
        System.out.println("4-" + new String(RsaUtils.decryptByRsaPrivate(secret, rsaEntity.getPrivateKeyModulusBytes(), rsaEntity.getPrivateKeyExponentBytes())));
    }

    private String encodeToBase64String(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    public void testEncryptAndDecrypt2() throws Exception {
        String content = "hello world";
        byte[] data = content.getBytes();
        byte[] secret;

        RsaUtils.RsaEntity rsaEntity = RsaUtils.generateRsaEntity();

        // 1.测试私钥加密，公钥解密(PKCS8, X509)
        secret = RsaUtils.encryptByRsaPrivate(data, encodeToBase64String(rsaEntity.getPrivateKeyBytes()));
        System.out.println("1-" + new String(RsaUtils.decryptByRsaPublic(secret, rsaEntity.getPublicKeyBytes())));

        // 2.测试公钥加密，私钥解密(PKCS8, X509)
        secret = RsaUtils.encryptByRsaPublic(data, encodeToBase64String(rsaEntity.getPublicKeyBytes()));
        System.out.println("2-" + new String(RsaUtils.decryptByRsaPrivate(secret, encodeToBase64String(rsaEntity.getPrivateKeyBytes()))));

        // 3.测试私钥加密，公钥解密({e,n})
        secret = RsaUtils.encryptByRsaPrivate(data, encodeToBase64String(rsaEntity.getPrivateKeyModulusBytes()), encodeToBase64String(rsaEntity.getPrivateKeyExponentBytes()));
        System.out.println("3-" + new String(RsaUtils.decryptByRsaPublic(secret, encodeToBase64String(rsaEntity.getPrivateKeyModulusBytes()), rsaEntity.getPublicKeyExponent())));

        // 4.测试公钥加密，私钥解密({e,n})
        secret = RsaUtils.encryptByRsaPublic(data, encodeToBase64String(rsaEntity.getPublicKeyModulusBytes()), rsaEntity.getPublicKeyExponent());
        System.out.println("4-" + new String(RsaUtils.decryptByRsaPrivate(secret, encodeToBase64String(rsaEntity.getPrivateKeyModulusBytes()), encodeToBase64String(rsaEntity.getPrivateKeyExponentBytes()))));
    }
}
