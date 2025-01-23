package com.example.chessandroid.ChessGame;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Objects;

@IgnoreExtraProperties
public class Move {
    private String action;
    private Piece from;
    private Piece to;
private Piece promotionPiece;
    public Move(Piece from, String action, Piece to) {
        this.action = action;
        this.from = from;
        this.to = to;
    }

    public Move(Piece from, String action) {
        this.from = from;
        this.action = action;
    }

    public Move( Piece from, String action, Piece to, Piece promotionPiece) {
        this.action = action;
        this.from = from;
        this.to = to;
        this.promotionPiece = promotionPiece;
    }

    public Piece getPromotionPiece() {
        return promotionPiece;
    }

    public void setPromotionPiece(Piece promotionPiece) {
        this.promotionPiece = promotionPiece;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setFrom(Piece from) {
        this.from = from;
    }

    public void setTo(Piece to) {
        this.to = to;
    }

    public Move() {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(action, move.action) && Objects.equals(from, move.from) && Objects.equals(to, move.to) && Objects.equals(promotionPiece, move.promotionPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, from, to, promotionPiece);
    }

    @NonNull
    @Override
    public String toString() {
        return "Move{" +
                "action='" + action + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", promotionPiece=" + promotionPiece +
                '}';
    }
}
