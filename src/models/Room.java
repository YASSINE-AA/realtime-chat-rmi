package models;

import java.io.Serializable;

public class Room implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String roomID;
    private final String creator;

    public Room(String roomID, String creator) {
        this.roomID = roomID;
        this.creator = creator;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getCreator() {
        return creator;
    }
}
