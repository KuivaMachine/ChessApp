package com.example.chessandroid.Model;

import com.example.chessandroid.ChessGame.Move;
import com.example.chessandroid.ChessGame.Piece;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashSet;

public interface FirebaseRepository {
    HashSet<Piece> getPieces();

    void addPiece(Piece piece);

    void removePiece(Piece piece);

    void sendLastMove(Move move);

    void setGameID(String gameID);

    void setIsInvertedBoard(boolean isInvertedBoard);

    boolean isInvertedBoard();

    String myColorOfPiece();

    void setMyColorOfPieces(String myColorOfPieces);

    void receiveMove(Callback callback);

    void deleteAllMovesHistory();

    void getUserData(UserDataCallback callback);

    void logIn(String email, String password, AuthCallback callback);

    FirebaseUser getCurrentUser();


    void sighUp(User user, String email, String password, AuthCallback callback);
}
