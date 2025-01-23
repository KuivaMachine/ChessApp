package com.example.chessandroid.Model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chessandroid.ChessGame.Coordinate;
import com.example.chessandroid.ChessGame.Move;
import com.example.chessandroid.ChessGame.Piece;
import com.example.chessandroid.enums.ColorOfPiece;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.LinkedList;

public class FirebaseRepositoryImpl implements FirebaseRepository {
    private String gameID;
    private boolean isInvertedBoard;
    private String myColorOfPieces;
    private final HashSet<Piece> pieces = new HashSet<>();
    private final LinkedList<Move> history = new LinkedList<>();
    private final String TAG = "MainActivity";
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://test-f992b-default-rtdb.europe-west1.firebasedatabase.app");
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public FirebaseRepositoryImpl() {
        pieces.add(new Piece("King Black", new Coordinate(8, 7), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("King White", new Coordinate(6, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Queen White", new Coordinate(1, 1), ColorOfPiece.BLACK, true));
/*
        pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Queen Black", new Coordinate(4, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Rook Black", new Coordinate(1, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Rook Black", new Coordinate(8, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Knight Black", new Coordinate(2, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Knight Black", new Coordinate(7, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Bishop Black", new Coordinate(3, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Bishop Black", new Coordinate(6, 8), ColorOfPiece.BLACK, true));

        pieces.add(new Piece("King White", new Coordinate(5, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Queen White", new Coordinate(4, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Rook White", new Coordinate(1, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Rook White", new Coordinate(8, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Knight White", new Coordinate(2, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Knight White", new Coordinate(7, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Bishop White", new Coordinate(3, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Bishop White", new Coordinate(6, 1), ColorOfPiece.WHITE, true));

        for (int i = 1; i <= 8; i++) {
            pieces.add(new Piece("Pawn Black", new Coordinate(i, 7), ColorOfPiece.BLACK, true));
            pieces.add(new Piece("Pawn White", new Coordinate(i, 2), ColorOfPiece.WHITE, true));
        }*/


    }

    public void receiveMove(Callback callback) {
        DatabaseReference game_data = database.getReference("Rooms").child(gameID).child("Moves " + getSecondPlayersColorOfPiece(myColorOfPieces));
        game_data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //  Log.d(TAG, "СЧИТЫВАЮ ИЗМЕНЕНИЯ В БД");
                Move move = dataSnapshot.getValue(Move.class);
                if (move != null) {
                    callback.getMove(move);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getDetails());
            }
        });

    }

    @Override
    public void deleteAllMovesHistory() {
        DatabaseReference game_data = database.getReference("Rooms");
        game_data.removeValue();
    }

    @Override
    public void getUserData(UserDataCallback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference reference = database.getReference().child("User").child(currentUser.getUid());
            reference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot data = task.getResult();
                    User user = data.getValue(User.class);
                    callback.getUserData(user);

                }
            });
        }
    }

    @Override
    public void logIn(String email, String password, AuthCallback callback) {
mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
    if(task.isSuccessful()){
        callback.returnAuthResult(task);
    }
});
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    @Override
    public void sighUp(User user, String email, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseDatabase.getInstance().getReference().child("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                           callback.returnAuthResult(task);
                        }
                    }
                });
    }


    public HashSet<Piece> getPieces() {
        return pieces;
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    public void removePiece(Piece piece) {
        pieces.remove(piece);
    }

    public void sendLastMove(Move move) {
        String movesPath = "Moves " + myColorOfPieces;
        DatabaseReference game_data = database.getReference("Rooms").child(gameID).child(movesPath);
        game_data.setValue(move);
    }


    @Override
    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    @Override
    public void setIsInvertedBoard(boolean isInvertedBoard) {
        this.isInvertedBoard = isInvertedBoard;
    }

    @Override
    public boolean isInvertedBoard() {
        return isInvertedBoard;
    }

    @Override
    public String myColorOfPiece() {
        return myColorOfPieces;
    }

    @Override
    public void setMyColorOfPieces(String myColorOfPieces) {
        this.myColorOfPieces = myColorOfPieces;
    }


    public String getSecondPlayersColorOfPiece(String myColorOfPiece) {
        if (myColorOfPiece.equals("White")) {
            return "Black";
        }
        return "White";
    }
}
