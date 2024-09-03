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
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {


    FloatingActionButton up;
    @SuppressLint("StaticFieldLeak")
    static TextView coor, move;
    static Board board;
    final private String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        board = findViewById(R.id.board_view);


        coor = findViewById(R.id.coor);
move = findViewById(R.id.whosMove);

     up = findViewById(R.id.btn);
        up.setOnClickListener((v) -> {
            Board.makeDefaultPlacement();

            board.invalidate();
        });




       /* down = findViewById(R.id.down_btn);
        right = findViewById(R.id.right_btn);
        left = findViewById(R.id.left_btn);



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


    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return super.onTouchEvent(event);
    }


}