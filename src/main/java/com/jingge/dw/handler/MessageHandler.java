package com.jingge.dw.handler;


public interface MessageHandler {

    void start();
    void stop();
    void dispose(byte[] message);
}
