package com.example.chessandroid.ChessGame;

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

    private Coordinate coordinates;
    private String name;
    private ColorOfPiece colorOfPiece;
    private boolean firstMove;

    public ColorOfPiece getColorOfPiece() {
        return colorOfPiece;
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

        //return "pieces.add(new Piece(\""+name + "\", new Coordinate(" + coordinates.getLetter()+","+coordinates.getNumber() + "), ColorOfPiece." + colorOfPiece+","+isFirstMove()+"));\n";
        return name + " " + coordinates;
    }

}
