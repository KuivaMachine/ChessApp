package com.example.chessandroid;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import android.widget.Button;

import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chessandroid.Pieces.Piece;


public class MainActivity extends AppCompatActivity {


   // Button up, down, right, left;
    @SuppressLint("StaticFieldLeak")
    static TextView coor;
    static Board board;
    final private String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        board = findViewById(R.id.board_view);


        coor = findViewById(R.id.coor);


       /* up = findViewById(R.id.up_btn);
        down = findViewById(R.id.down_btn);
        right = findViewById(R.id.right_btn);
        left = findViewById(R.id.left_btn);


        up.setOnClickListener((v) -> {
            makeMove(new Piece("King White", new Coordinate(Coordinate.lettersArray[Board.pieces.get(0).getCoordinates().getLetter()], Board.pieces.get(0).getCoordinates().number + 1), ColorWB.WHITE));
        });
        down.setOnClickListener((v) -> {
            makeMove(new Piece("King White", new Coordinate(Coordinate.lettersArray[Board.pieces.get(0).getCoordinates().getLetter()], Board.pieces.get(0).getCoordinates().number - 1), ColorWB.WHITE));
        });
        right.setOnClickListener((v) -> {
            makeMove(new Piece("King White", new Coordinate(Coordinate.lettersArray[Board.pieces.get(0).getCoordinates().getLetter() + 1], Board.pieces.get(0).getCoordinates().number), ColorWB.WHITE));
        });
        left.setOnClickListener((v) -> {
            makeMove(new Piece("King White", new Coordinate(Coordinate.lettersArray[Board.pieces.get(0).getCoordinates().getLetter() - 1], Board.pieces.get(0).getCoordinates().number), ColorWB.WHITE));
        });*/


    }


    public void setTextOfCoors() {
        coor.setText(String.format("%s%s", Board.pieces.get(0).getCoordinates().letter, Board.pieces.get(0).getCoordinates().number));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        coor.setText(String.format("%s\n%s", (int) event.getX(), (int) event.getY()));

        return super.onTouchEvent(event);
    }


}