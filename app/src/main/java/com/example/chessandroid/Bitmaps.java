package com.example.chessandroid;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.view.View;


import java.util.HashMap;


public class Bitmaps extends View {
    static HashMap<String, Bitmap> listOfPiecesAndPNG = new HashMap<>();
    static HashMap<String, String> colors = new HashMap<>();
    public Bitmaps(Context context) {
        super(context);


        switch (Board.INDEX_OF_COLOR) {

            case (1):
                listOfPiecesAndPNG.put("King White", BitmapFactory.decodeResource(getResources(), R.drawable.king_white));
                listOfPiecesAndPNG.put("King Black", BitmapFactory.decodeResource(getResources(), R.drawable.king_black));
                listOfPiecesAndPNG.put("Queen White", BitmapFactory.decodeResource(getResources(), R.drawable.queen_white));
                listOfPiecesAndPNG.put("Queen Black", BitmapFactory.decodeResource(getResources(), R.drawable.queen_black));
                listOfPiecesAndPNG.put("Rook White", BitmapFactory.decodeResource(getResources(), R.drawable.rook_white));
                listOfPiecesAndPNG.put("Rook Black", BitmapFactory.decodeResource(getResources(), R.drawable.rook_black));
                listOfPiecesAndPNG.put("Knight White", BitmapFactory.decodeResource(getResources(), R.drawable.knight_white));
                listOfPiecesAndPNG.put("Knight Black", BitmapFactory.decodeResource(getResources(), R.drawable.knight_black));
                listOfPiecesAndPNG.put("Bishop White", BitmapFactory.decodeResource(getResources(), R.drawable.bishop_white));
                listOfPiecesAndPNG.put("Bishop Black", BitmapFactory.decodeResource(getResources(), R.drawable.bishop_black));
                listOfPiecesAndPNG.put("Pawn White", BitmapFactory.decodeResource(getResources(), R.drawable.pawn_white));
                listOfPiecesAndPNG.put("Pawn Black", BitmapFactory.decodeResource(getResources(), R.drawable.pawn_black));

                colors.put("Первый цвет клетки", "#3B873D");
                colors.put("Второй цвет клетки", "#FEFFE3");
                colors.put("Цвет выделения свободного хода", "#CCCCCC");
                break;
            case (2):
                listOfPiecesAndPNG.put("King White", BitmapFactory.decodeResource(getResources(), R.drawable.king_white_red));
                listOfPiecesAndPNG.put("King Black", BitmapFactory.decodeResource(getResources(), R.drawable.king_black_red));
                listOfPiecesAndPNG.put("Queen White", BitmapFactory.decodeResource(getResources(), R.drawable.queen_white_red));
                listOfPiecesAndPNG.put("Queen Black", BitmapFactory.decodeResource(getResources(), R.drawable.queen_black_red));
                listOfPiecesAndPNG.put("Rook White", BitmapFactory.decodeResource(getResources(), R.drawable.rook_white_red));
                listOfPiecesAndPNG.put("Rook Black", BitmapFactory.decodeResource(getResources(), R.drawable.rook_black_red));
                listOfPiecesAndPNG.put("Knight White", BitmapFactory.decodeResource(getResources(), R.drawable.knight_white_red));
                listOfPiecesAndPNG.put("Knight Black", BitmapFactory.decodeResource(getResources(), R.drawable.knight_black_red));
                listOfPiecesAndPNG.put("Bishop White", BitmapFactory.decodeResource(getResources(), R.drawable.bishop_white_red));
                listOfPiecesAndPNG.put("Bishop Black", BitmapFactory.decodeResource(getResources(), R.drawable.bishop_black_red));
                listOfPiecesAndPNG.put("Pawn White", BitmapFactory.decodeResource(getResources(), R.drawable.pawn_white_red));
                listOfPiecesAndPNG.put("Pawn Black", BitmapFactory.decodeResource(getResources(), R.drawable.pawn_black_red));

                colors.put("Первый цвет клетки", "#F4A68C");
                colors.put("Второй цвет клетки", "#FAD7D7");
                colors.put("Цвет выделения свободного хода", "#AD2143");
                break;

            case (3):
                listOfPiecesAndPNG.put("King White", BitmapFactory.decodeResource(getResources(), R.drawable.king_white_new));
                listOfPiecesAndPNG.put("King Black", BitmapFactory.decodeResource(getResources(), R.drawable.king_black_new));
                listOfPiecesAndPNG.put("Queen White", BitmapFactory.decodeResource(getResources(), R.drawable.queen_white_new));
                listOfPiecesAndPNG.put("Queen Black", BitmapFactory.decodeResource(getResources(), R.drawable.queen_black_new));
                listOfPiecesAndPNG.put("Rook White", BitmapFactory.decodeResource(getResources(), R.drawable.rook_white_new));
                listOfPiecesAndPNG.put("Rook Black", BitmapFactory.decodeResource(getResources(), R.drawable.rook_black_new));
                listOfPiecesAndPNG.put("Knight White", BitmapFactory.decodeResource(getResources(), R.drawable.knight_white_new));
                listOfPiecesAndPNG.put("Knight Black", BitmapFactory.decodeResource(getResources(), R.drawable.knight_black_new));
                listOfPiecesAndPNG.put("Bishop White", BitmapFactory.decodeResource(getResources(), R.drawable.bishop_white_new));
                listOfPiecesAndPNG.put("Bishop Black", BitmapFactory.decodeResource(getResources(), R.drawable.bishop_black_new));
                listOfPiecesAndPNG.put("Pawn White", BitmapFactory.decodeResource(getResources(), R.drawable.pawn_white_new));
                listOfPiecesAndPNG.put("Pawn Black", BitmapFactory.decodeResource(getResources(), R.drawable.pawn_black_new));

                colors.put("Первый цвет клетки", "#3B873D");
                colors.put("Второй цвет клетки", "#FEFFE3");
                colors.put("Цвет выделения свободного хода", "#999999");
                break;
        }
    }


}
