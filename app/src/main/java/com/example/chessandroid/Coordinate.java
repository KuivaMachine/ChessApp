package com.example.chessandroid;

import java.util.ArrayList;
import java.util.Objects;

public class Coordinate {



    int letter;
    int number;

    public Coordinate(int letter, int number) {
        this.letter = letter;
        this.number = number;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return letter == that.letter && number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(letter, number);
    }

    @Override
    public String toString() {
        return  letter +
                " " + number ;
    }
}

