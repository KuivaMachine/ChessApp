package com.example.chessandroid.classes;

import com.example.chessandroid.enums.Players;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Room {
    private User player_1;
    private User player_2;
    private boolean isRoomFull;
    private Players who_is_white;

    public Room() {
    }

    public Room(User player_1, User player_2, boolean isRoomFull, Players who_is_white) {
        this.player_1 = player_1;
        this.player_2 = player_2;
        this.isRoomFull = isRoomFull;
        this.who_is_white = who_is_white;
    }

    public User getPlayer_1() {
        return player_1;
    }

    public void setPlayer_1(User player_1) {
        this.player_1 = player_1;
    }

    public User getPlayer_2() {
        return player_2;
    }

    public void setPlayer_2(User player_2) {
        this.player_2 = player_2;
    }

    public boolean isRoomFull() {
        return isRoomFull;
    }

    public void setRoomFull(boolean roomFull) {
        isRoomFull = roomFull;
    }

    public Players getWho_is_white() {
        return who_is_white;
    }

    public void setWho_is_white(Players who_is_white) {
        this.who_is_white = who_is_white;
    }

    @Override
    public String toString() {
        return "Room{" +
                "player_1=" + player_1 +
                ", player_2=" + player_2 +
                ", isRoomFull=" + isRoomFull +
                ", who_is_white=" + who_is_white +
                '}';
    }
}
