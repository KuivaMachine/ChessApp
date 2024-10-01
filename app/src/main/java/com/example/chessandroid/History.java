package com.example.chessandroid;

public class History {
    private String action;
    private Piece from;
    private Piece to;

    public History( Piece from,String action, Piece to) {
        this.action = action;
        this.from = from;
        this.to = to;
    }

    public String getAction(){
        return this.action;
    }

    public Piece getFrom() {
        return from;
    }

    public Piece getTo() {
        return to;
    }
}
