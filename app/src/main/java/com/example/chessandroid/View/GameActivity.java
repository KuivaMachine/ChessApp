package com.example.chessandroid.View;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.example.chessandroid.ChessGame.Move;
import com.example.chessandroid.R;
import com.example.chessandroid.View.Fragments.EnemyFragment;
import com.example.chessandroid.ViewModel.ChessViewModel;
import com.example.chessandroid.databinding.ActivityGameBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


//TODO: пат и ничья (stalemate and draw)
//TODO: Жизненный цикл игры
//TODO: Возможность меняться цветом фигур


public class GameActivity extends AppCompatActivity {

    private String gameID;
    private String myColorOfPieces;
    private String player_2_nick;
    private boolean isInvertedBoard;
    private ActivityGameBinding binding;
    final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Board board = new Board(GameActivity.this, null);

        //ИНИЦИАЛИЗАЦИЯ ДАННЫХ, ПОЛУЧЕННЫХ ПОСЛЕ СОЗДАНИЯ ИГРЫ
        Bundle data = getIntent().getExtras();
        if (data != null) {
            this.gameID = data.getString("gameID");
            this.isInvertedBoard = data.getBoolean("isInvertedBoard");
            this.myColorOfPieces = data.getString("myColorOfPieces");
            this.player_2_nick = data.getString("player_2_nick");
        }

        EnemyFragment enemyFragment = new EnemyFragment(player_2_nick);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, enemyFragment).commit();


        ChessViewModel viewModel = new ViewModelProvider(this).get(ChessViewModel.class);
        viewModel.setGameID(gameID);
        viewModel.setIsInvertedBoard(isInvertedBoard);
        viewModel.setMyColorOfPiece(myColorOfPieces);


        viewModel.loadMove();

        viewModel.receiveMove().observe(this, new Observer<Move>() {
            @Override
            public void onChanged(Move move) {
                if (!viewModel.isMyTurn() && move != null) {
                    viewModel.makeForwardMoveByHistory(move);
                }

            }
        });

        viewModel.isMyTurnNow().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isMyTurn) {
                if (isMyTurn) {
                    binding.whosMove.setText("Твой ход");
                } else {
                    binding.whosMove.setText("Ожидание соперника");
                }
                viewModel.isWhiteMoving();
            }
        });
        viewModel.isCheckMateBlackNow().observe(this, isCheckMateBlack -> {
            if (isCheckMateBlack) {
                binding.checkText.setText("Мат черным");
            }
        });

        viewModel.isCheckMateWhiteNow().observe(this, isCheckMateWhite -> {
            if (isCheckMateWhite) {
                binding.checkText.setText("Мат белым");
            }
        });
        viewModel.isStalemateNow().observe(this, isStalemateNow -> {
            if (isStalemateNow) {
                binding.checkText.setText("ПАТ");
            }
        });

        binding.backBtn.setOnClickListener(v -> {
//            if (!ChessGame.history.isEmpty()) {
//                chessGame.makeBackMoveByHistory(ChessGame.move.getLast());
//                chessGame.movesBuffer.addFirst(ChessGame.move.getLast());
//                ChessGame.move.removeLast();
//                board.invalidate();
//            } else {
//                Log.d(TAG, ChessGame.history.toString());
//            }
            viewModel.deleteAllMovesHistory();
        });

        binding.forwardBtn.setOnClickListener(v -> {
//            if (!chessGame.movesBuffer.isEmpty()) {
//                chessGame.makeForwardMoveByHistory(chessGame.movesBuffer.getFirst());
//                ChessGame.move.add(chessGame.movesBuffer.getFirst());
//                chessGame.movesBuffer.removeFirst();
//                board.invalidate();
//            } else {
//                Log.d(TAG, ChessGame.history.toString());
//            }
        });

        binding.checkmateBtn.setOnClickListener(v -> {
            board.invalidate();
        });

        binding.saveBtn.setOnClickListener(v -> {

        });


    }


    /*public static void sendMove(History move){
       game_data = database.getReference(gameID).child("Moves");
        game_data.setValue(move);
    }*/


/*

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Bundle data = getIntent().getExtras();
        if (data != null) {
            gameID = data.getString("gameID");
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference game_data = database.getReference("Rooms").child(gameID).child("Moves " + myColorOfPieces);
        game_data.removeValue();
    }
}