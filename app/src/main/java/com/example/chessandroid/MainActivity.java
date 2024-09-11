package com.example.chessandroid;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;

import android.view.MotionEvent;

import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    FloatingActionButton reloadButton, invertButton, back;
    @SuppressLint("StaticFieldLeak")
    static TextView coor, move, checkmate;
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
        checkmate = findViewById(R.id.checkmate);


        reloadButton = findViewById(R.id.btn_reload);
        reloadButton.setOnClickListener((v) -> {
            Board.makeDefaultPlacement();
            board.invalidate();
        });


        invertButton = findViewById(R.id.btn_invert);
        invertButton.setOnClickListener((v) -> {
            Board.isInvertedBoard = !Board.isInvertedBoard;
            board.invalidate();
        });

        back = findViewById(R.id.back_btn);
        back.setOnClickListener(v -> {
            if (!Board.moveRecord.isEmpty()) {
                board.makeBackMove(Board.moveRecord.getLast());
                Board.moveRecord.removeLast();
            }
        });

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return super.onTouchEvent(event);
    }


}