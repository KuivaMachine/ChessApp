package com.example.chessandroid.ChessGame;

import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chessandroid.R;
import com.example.chessandroid.classes.ChessGame;
import com.example.chessandroid.databinding.ActivityGameBinding;
import com.example.chessandroid.enums.ColorOfPiece;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Target;


public class GameActivity extends AppCompatActivity {
    static Board board;
    ChessGame chessGame = new ChessGame();
    static String gameID;
    static boolean isInvertedBoard;
    ColorOfPiece myColorOfPiece;
    private ActivityGameBinding binding;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
    final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        board = findViewById(R.id.board_view);

        //ИНИЦИАЛИЗАЦИЯ ДАННЫХ, ПОЛУЧЕННЫХ ПОСЛЕ СОЗДАНИЯ ИГРЫ
        Bundle data = getIntent().getExtras();
        if (data != null) {
            gameID = data.getString("gameID");
            isInvertedBoard = data.getBoolean("isInvertedBoard");
            myColorOfPiece = data.getSerializable("myColorOfPieces", ColorOfPiece.class);
            board.invalidate();
        }

        DatabaseReference game_data = database.getReference(gameID).child("Moves");

        game_data.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "СЧИТЫВАЮ ИЗМЕНЕНИЯ В БД");
                History history = dataSnapshot.getValue(History.class);

                if (history != null) {
                    Log.d(TAG, history.toString());
                    chessGame.makeForwardMoveByHistory(history);
                } else {
                    Log.d(TAG, "history = NULL");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GameActivity.this, "Ошибка чтения хода из сервера", Toast.LENGTH_SHORT).show();
            }
        });


        binding.btnReload.setOnClickListener((v) -> {

            chessGame.makeDefaultPlacement();
            binding.countW.setText("");
            binding.countB.setText("");
            binding.checkmate.setText("");
            board.invalidate();
        });

        binding.btnInvert.setOnClickListener((v) -> {
            isInvertedBoard = !isInvertedBoard;
            board.invalidate();
        });

        binding.backBtn.setOnClickListener(v -> {
         /*   if (!chessGame.history.isEmpty()) {
                chessGame.makeBackMoveByHistory(chessGame.history.getLast());
                chessGame.movesBuffer.addFirst(chessGame.history.getLast());
                chessGame.history.removeLast();
            }*/
        });

        binding.forwardBtn.setOnClickListener(v -> {
            /*if (!chessGame.movesBuffer.isEmpty()) {
                chessGame.makeForwardMoveByHistory(chessGame.movesBuffer.getFirst());
                chessGame.history.add(chessGame.movesBuffer.getFirst());
                chessGame.movesBuffer.removeFirst();
            }*/
        });

        binding.checkmateBtn.setOnClickListener(v -> {
        });

        binding.saveBtn.setOnClickListener(v -> {
            Log.d(TAG, chessGame.history.toString());
            // game_data.setValue(new History(new Piece("Empty Piece", new Coordinate(0, 0), ColorOfPiece.TEST, false), "eat", new Piece("Empty Piece", new Coordinate(0, 0), ColorOfPiece.TEST, false)));
        });


    }

    public static void sendMove(History history) {
//        FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
//        DatabaseReference game_data = database.getReference(gameID).child("Moves");
//        game_data.setValue(history);
    }

    ;

    /*public static void sendMove(History move){
       game_data = database.getReference(gameID).child("Moves");
        game_data.setValue(move);
    }*/
  /*  public void isMyMove() {
        if ((myColorOfPiece == ColorOfPiece.WHITE && Board.isWhiteMoving) || (myColorOfPiece == ColorOfPiece.BLACK && !Board.isWhiteMoving)) {
            Log.d(TAG, "Сейчас мой ход");
           // return true;
        } else {
            Log.d(TAG, "Сейчас не мой ход");
           // return false;
        }
    }
*/
   /* public void writeWhoIsMove() {//ПИШЕТ КОГДА ЧЕЙ ХОД
        if (Board.isWhiteMoving) {
            binding.whosMove.setText("Белые ходят");
        } else {
            binding.whosMove.setText("Черные ходят");
        }
    }

    public void setCheckMateText(boolean isCheckMate) {
        if (isCheckMate) {
            if (!Board.isCheckTheWhite) {
                binding.checkmate.setText("Мат черным!");
            }
            if (Board.isCheckTheWhite) {
                binding.checkmate.setText("Мат белым!");
            }
        }
    }

    public void setCheckText(boolean isCheck) {
        if (isCheck) {

            if (!Board.isCheckTheWhite) {
                binding.checkmate.setText("Шах черным!");
            }
            if (Board.isCheckTheWhite) {
                binding.checkmate.setText("Шах белым!");
            }
        }
    }*/

}