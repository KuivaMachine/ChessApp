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
        switch (letter){
            case(1):
                return  'A' +
                        "" + number ;
            case (2):
                return  'B' +
                        "" + number ;
            case (3):
                return  'C' +
                        "" + number ;
            case (4):
                return  'D' +
                        "" + number ;
            case (5):
                return  'E' +
                        "" + number ;
            case (6):
                return  'F' +
                        "" + number ;
            case (7):
                return  'G' +
                        "" + number ;
            case (8):
                return  'H' +
                        "" + number ;
        }
       return letter+" "+number;
    }
}

