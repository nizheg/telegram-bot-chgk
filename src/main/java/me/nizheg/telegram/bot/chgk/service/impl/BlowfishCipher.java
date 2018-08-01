package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import me.nizheg.telegram.bot.chgk.exception.CipherException;
import me.nizheg.telegram.bot.chgk.service.Cipher;

/**
 * @author Nikolay Zhegalin
 */
@Component
public class BlowfishCipher implements Cipher {

    public static final String CIPHER_METHOD = "Blowfish";
    private final SecretKey secretKey;

    public BlowfishCipher() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(CIPHER_METHOD);
            secretKey = keyGenerator.generateKey();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String encrypt(String source) throws CipherException {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_METHOD);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
            return DatatypeConverter.printHexBinary(cipher.doFinal(source.getBytes())).toLowerCase();
        } catch (Exception e) {
            throw new CipherException(e);
        }
    }

    @Override
    public String decrypt(String encrypted) throws CipherException {
        try {
            javax.crypto.Cipher decryptor = javax.crypto.Cipher.getInstance(CIPHER_METHOD);
            decryptor.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
            return new String(decryptor.doFinal(DatatypeConverter.parseHexBinary(encrypted)));
        } catch (Exception e) {
            throw new CipherException(e);
        }
    }

}
