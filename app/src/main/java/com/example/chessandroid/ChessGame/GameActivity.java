package com.example.chessandroid.ChessGame;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chessandroid.Activities.MenuActivity;
import com.example.chessandroid.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GameActivity extends AppCompatActivity {
    static String gameID = "0000";

    FloatingActionButton reloadButton, invertButton, back, checkmate_But, save_but, forward_but;

    TextView countW, countB, move, checkmate;
    static Board board;
    final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        board = findViewById(R.id.board_view);

        countW = findViewById(R.id.countW);
        countB = findViewById(R.id.countB);

        Bundle data = getIntent().getExtras();
        if (data != null) {
            gameID = data.getString("gameID");
            Board.isInvertedBoard=data.getBoolean("isInvertedBoard");
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference(gameID).child("Moves");

       myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                History history = dataSnapshot.getValue(History.class);
                if (history != null) {
                    board.makeForwardMoveByHistory(history);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GameActivity.this, "Ошибка чтения хода из сервера", Toast.LENGTH_SHORT).show();
            }
        });


        move = findViewById(R.id.whosMove);
        move.setText(Board.move);
        checkmate = findViewById(R.id.checkmate);
        checkmate.setText(Board.checkmate);

        reloadButton = findViewById(R.id.btn_reload);
        reloadButton.setOnClickListener((v) -> {
            Board.makeDefaultPlacement();
            countW.setText("");
            countB.setText("");
            checkmate.setText(Board.checkmate);
            board.invalidate();
        });


        invertButton = findViewById(R.id.btn_invert);
        invertButton.setOnClickListener((v) -> {
            Board.isInvertedBoard = !Board.isInvertedBoard;
            board.invalidate();
        });

        back = findViewById(R.id.back_btn);
        back.setOnClickListener(v -> {
            if (!Board.history.isEmpty()) {
                board.makeBackMoveByHistory(Board.history.getLast());
                Board.movesBuffer.addFirst(Board.history.getLast());
                Board.history.removeLast();
            }
        });

        forward_but = findViewById(R.id.forward_btn);
        forward_but.setOnClickListener(v -> {

            if (!Board.movesBuffer.isEmpty()) {
                board.makeForwardMoveByHistory(Board.movesBuffer.getFirst());
                Board.history.add(Board.movesBuffer.getFirst());
                Board.movesBuffer.removeFirst();
            }
        });

        checkmate_But = findViewById(R.id.checkmate_btn);
        checkmate_But.setOnClickListener(v -> {

        });

        save_but = findViewById(R.id.save_btn);
        save_but.setOnClickListener(v -> {
            myRef.setValue(Board.history.getLast());


        });


    }

}