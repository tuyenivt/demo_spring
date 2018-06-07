package com.coloza.sample;

import java.io.Serializable;

public class RelyObjectMessage implements Serializable {

    private static final long serialVersionUID = -7017298286219753811L;

    private int id;
    private String relyMessage;

    public RelyObjectMessage(int id, String relyMessage) {
        this.id = id;
        this.relyMessage = relyMessage;
    }

    @Override
    public String toString() {
        return "RelyObjectMessage [id=" + id + ", relyMessage=" + relyMessage + "]";
    }

}
