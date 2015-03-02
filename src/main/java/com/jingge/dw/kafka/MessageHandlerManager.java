package com.jingge.dw.kafka;

import com.jingge.dw.handler.MessageHandler;
import io.dropwizard.lifecycle.Managed;

import java.util.List;

/**
 * Created by jing.ge on 12.11.14.
 */
public class MessageHandlerManager implements Managed {
    private List<MessageHandler> messageHandlers;

    public MessageHandlerManager(List<MessageHandler> messageHandlers) {
        this.messageHandlers = messageHandlers;
    }

    @Override
    public void start() throws Exception {
        for (MessageHandler messagehandler: messageHandlers) {
            messagehandler.start();
        }
    }

    @Override
    public void stop() throws Exception {
        for (MessageHandler messagehandler : messageHandlers) {
            messagehandler.stop();
        }
    }
}
