package com.airondlph.economy.household.util;


import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author adriandlph / airondlph
 */
public class Crypton {

    public static RSAPublicKey ReadX509PublicKey(String filename) throws Exception {
        if (filename == null) throw new Exception("File does not exists.");

        File file = new File(filename);
        if (!file.exists() || !file.isFile()) throw new Exception("File does not exists.");

        try {
            // Read all file
            String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

            // Clean key
            String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

            // Encode key
            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);

        } catch (Exception ex) {
            throw new Exception("Error decoding key file.", ex);
        }
    }


    public static RSAPrivateKey ReadPKCS8PrivateKey(String filename) throws Exception {
        if (filename == null) throw new Exception("File does not exists.");

        File file = new File(filename);
        if (!file.exists() || !file.isFile()) throw new Exception("File does not exists.");

        try {
            // Read all file
            String key = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());

            // Clean key
            String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

            // Encode key
            byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

        } catch (Exception ex) {
            throw new Exception("Error decoding key file.", ex);
        }
    }
}
