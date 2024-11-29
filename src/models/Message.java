package models;

import java.io.Serializable;

public class Message implements Serializable {
    private final String senderID;
    private final String roomID;
    private final String content;

    public Message(String senderID, String roomID, String content) {
        this.senderID = senderID;
        this.roomID = roomID;
        this.content = content;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getContent() {
        return content;
    }
}
