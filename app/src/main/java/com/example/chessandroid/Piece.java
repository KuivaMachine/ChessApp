package com.example.chessandroid;

import com.example.chessandroid.enums.ColorOfPiece;

import java.util.Objects;

public class Piece {
    public Piece(String name, Coordinate coordinates, ColorOfPiece colorOfPiece, boolean firstMove) {
        this.colorOfPiece = colorOfPiece;
        this.name = name;
        this.coordinates = coordinates;
        this.firstMove = firstMove;
    }

    public Piece() {
    }

    public String getName() {
        return name;
    }

    Coordinate coordinates;
    String name;
    ColorOfPiece colorOfPiece;
    boolean firstMove;

    public void setColorOfPiece(ColorOfPiece colorOfPiece) {
        this.colorOfPiece = colorOfPiece;
    }

    public ColorOfPiece getColorOfPiece() {
        return colorOfPiece;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public boolean isFirstMove() {
        return firstMove;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return firstMove == piece.firstMove && Objects.equals(coordinates, piece.coordinates) && Objects.equals(name, piece.name) && colorOfPiece == piece.colorOfPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, name, colorOfPiece, firstMove);
    }

    @Override
    public String toString() {
       // pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorOfPiece.BLACK, true));
        //pieces.add(new Piece(Pawn White, new Coordinate(4,4),WHITE,false
        return "pieces.add(new Piece(\""+name + "\", new Coordinate(" + coordinates.getLetter()+","+coordinates.getNumber() + "), ColorOfPiece." + colorOfPiece+","+isFirstMove()+"));\n";
    }

}
