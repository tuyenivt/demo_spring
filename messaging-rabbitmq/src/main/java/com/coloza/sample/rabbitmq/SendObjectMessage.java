package com.coloza.sample.rabbitmq;

import java.io.Serializable;

public class SendObjectMessage implements Serializable {

    private static final long serialVersionUID = 1017393739480308008L;

    private int id;
    private String sendMessage;

    public SendObjectMessage(int id, String sendMessage) {
        this.id = id;
        this.sendMessage = sendMessage;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SendObjectMessage [id=" + id + ", sendMessage=" + sendMessage + "]";
    }

}
