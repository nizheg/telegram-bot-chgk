package me.nizheg.telegram.bot.chgk.service;

public interface MessageReceiverService {
    void startReceiving(long delayInMs);

    void stopReceiving();
}
