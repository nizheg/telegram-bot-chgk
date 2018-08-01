package me.nizheg.telegram.bot.chgk.service;

import me.nizheg.telegram.bot.chgk.exception.CipherException;

public interface Cipher {

    String encrypt(String source) throws CipherException;

    String decrypt(String encrypted) throws CipherException;
}
