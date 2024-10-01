package com.example.chessandroid;


import java.util.Objects;

public class Coordinate {

    private int letter;
    private int number;

    public Coordinate(int letter, int number) {
        this.letter = letter;
        this.number = number;

    }

    public int getLetter() {
        return letter;
    }

    public int getNumber() {
        return number;
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
        return letter +""+number;
    }
/* @Override
    public String toString() {
        switch (letter){
            case(1):
                return  'a' +
                        "" + number ;
            case (2):
                return  'b' +
                        "" + number ;
            case (3):
                return  'c' +
                        "" + number ;
            case (4):
                return  'd' +
                        "" + number ;
            case (5):
                return  'e' +
                        "" + number ;
            case (6):
                return  'f' +
                        "" + number ;
            case (7):
                return  'g' +
                        "" + number ;
            case (8):
                return  'h' +
                        "" + number ;
        }
       return letter+" "+number;
    }*/
}

