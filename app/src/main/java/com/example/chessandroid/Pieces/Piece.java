package com.example.chessandroid.Pieces;

import com.example.chessandroid.ColorWB;
import com.example.chessandroid.Coordinate;

import java.util.Objects;

public class Piece {
    public Piece(String name, Coordinate coordinates, ColorWB colorWB, boolean firstMove) {
        this.colorWB = colorWB;
        this.name = name;
        this.coordinates = coordinates;
        this.firstMove= firstMove;
    }


    public String getName() {
        return name;
    }

    Coordinate coordinates;
    String name;
    ColorWB colorWB;
    boolean firstMove;

    public ColorWB getColorWB() {
        return colorWB;
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
        return firstMove == piece.firstMove && Objects.equals(coordinates, piece.coordinates) && Objects.equals(name, piece.name) && colorWB == piece.colorWB;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, name, colorWB, firstMove);
    }

    @Override
    public String toString() {
        return coordinates + " " + name + " "+ colorWB;
    }


}
