package models;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    public final String source;
    public final String destination;
    public final String content;

    public Message(String source, String destination, String content) {
        this.source = source;
        this.destination = destination;
        this.content = content;
    }
}
