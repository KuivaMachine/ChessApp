package com.example.chessandroid.ChessGame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.example.chessandroid.enums.ColorOfPiece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;


//TODO: сохранение взятых фигур
//TODO: Рокировка (castling)
//TODO: Взятие на проходе (en passant)
//TODO: Превращение пешки (promotion)
//TODO: буквы и цифры
//TODO: пат и ничья (stalemate and draw)

public class Board extends View {

    Piece bufferPiece = new Piece();
    Piece testPiece = new Piece("EmptyPlace", new Coordinate(8, 8), ColorOfPiece.TEST, false);



    //ПЕРЕМЕННЫЕ

    final private String TAG = "MainActivity";
    float scaleFactor = 1f;                   //коэффициент размера доски
    float chessBoardSize;                       //размер всей доски
    int squareSide;                         //размер квадрата
    float defY;                          //координата У для начала отсчета доски
    float defX;                            //координата Х для начала отсчета доски
    float radius0fFreeMovePoint = 18;    //радиус точки, указывающей на доступные ходы
    float indent = 5;                   //размер отступа внутри клетки до фигуры
    static int INDEX_OF_COLOR = 3;          //вариант дизайна фигур
    static boolean isWhiteMoving;       //проверка на очередность ходов
    static boolean isInvertedBoard = false; //проверка, является ли доска инвертированной
    static boolean isCheck;         //флаг шаха
    static boolean isCheckMate;         //флаг мата
    static boolean isCheckTheWhite;         //шах белому королю
    static boolean isCheckTheBlack;         //шах черному королю
    static String checkmate = "";
    static String move="";
    ColorOfPiece checkColor;                    //цвет фигуры, которой шах

    //МАССИВЫ И ЛИСТЫ
    static ArrayList<Integer> timeDrawMoves = new ArrayList<>();
    static ArrayList<Piece> pieces = new ArrayList<>();
    static ArrayList<Piece> takenPiecesWhite = new ArrayList<>();
    static ArrayList<Piece> takenPiecesBlack = new ArrayList<>();
    static LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();

    static LinkedHashMap<Coordinate, RectF> takenPiecesRects = new LinkedHashMap<>();

    static HashSet<Coordinate> allowedMovesList = new HashSet<>();
    static HashSet<Coordinate> busyCoordinates = new HashSet<>();
    static LinkedList<History> history = new LinkedList<>();
    static LinkedList<History> movesBuffer = new LinkedList<>();
    HashMap<String, Bitmap> listOfPiecesAndPNG = Bitmaps.listOfPiecesAndPNG;
    HashMap<String, Bitmap> listOfTakenPiecesAndPNG = Bitmaps.listOfTakenPiecesAndPNG;
    HashMap<String, String> colors = Bitmaps.colors;

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makeDefaultPlacement();

        new Bitmaps(this.getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!checkBordersOfView(event.getX(), event.getY())) {
                Coordinate currentCoor = invertedCoordinates(event.getX(), event.getY());

                if (getPieceAtCoordinates(currentCoor).getColorOfPiece() == ColorOfPiece.WHITE && isWhiteMoving) {
                    bufferPiece = getPieceAtCoordinates(currentCoor);
                    //long startTime = System.currentTimeMillis();
                    canThePieceMove(bufferPiece, allowedMovesList);
                    multyMove(bufferPiece, allowedMovesList);
                    // long endTime = System.currentTimeMillis();
                    // timeDrawMoves.add((int) (endTime - startTime));
                }
                if (getPieceAtCoordinates(currentCoor).getColorOfPiece() == ColorOfPiece.BLACK && !isWhiteMoving) {
                    bufferPiece = getPieceAtCoordinates(currentCoor);
                    canThePieceMove(bufferPiece, allowedMovesList);
                    multyMove(bufferPiece, allowedMovesList);
                }
                if (isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece sacrificePiece = getPieceAtCoordinates(currentCoor);
                    eatThePiece(bufferPiece, sacrificePiece);
                    addTakenPiece(sacrificePiece);
                    history.add(new History(bufferPiece, "eat", sacrificePiece));
                    afterMove();
                    movesBuffer.clear();
                }
                if (!isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece gotoPiece = new Piece(bufferPiece.getName(), currentCoor, bufferPiece.getColorOfPiece(), false);
                    makeMove(bufferPiece, gotoPiece);
                    history.add(new History(bufferPiece, "move", gotoPiece));
                    afterMove();
                    movesBuffer.clear();
                }
                if (!isPieceAtCoordinates(currentCoor)) {
                    allowedMovesList.clear();
                }
                invalidate();
            }
        }
        return true;
    }

    public void makeBackMoveByHistory(History lastAction) {
        switch (lastAction.getAction()) {
            case ("move"):
                Piece from = lastAction.getFrom();
                Piece to = lastAction.getTo();
                makeBackMove(from, to);
                break;

            case ("eat"):
                Piece attackPiece = lastAction.getFrom();
                Piece sacrificePiece = lastAction.getTo();
                makeBackEat(attackPiece, sacrificePiece);
                if(sacrificePiece.getColorOfPiece().equals(ColorOfPiece.WHITE)){
                    takenPiecesWhite.remove(takenPiecesWhite.size()-1);
                }else{
                    takenPiecesBlack.remove(takenPiecesBlack.size()-1);
                }
                break;
        }

        afterMove();
    }

    public void makeForwardMoveByHistory(History lastAction) {

        switch (lastAction.getAction()) {
            case ("move"):
                Piece from = lastAction.getFrom();
                Piece to = lastAction.getTo();
                makeMove(from, to);
                break;

            case ("eat"):
                Piece attackPiece = lastAction.getFrom();
                Piece sacrificePiece = lastAction.getTo();
                eatThePiece(attackPiece, sacrificePiece);
                addTakenPiece(sacrificePiece);
                break;
        }
        afterMove();
    }

    private void makeMove(Piece fromPiece, Piece gotoPiece) {
        pieces.remove(fromPiece);
        pieces.add(gotoPiece);
        fillBusyCoordinatesList();
    }


    public void makeBackMove(Piece fromPiece, Piece gotoPiece) {
        pieces.remove(gotoPiece);
        pieces.add(fromPiece);
        fillBusyCoordinatesList();
    }

    private void eatThePiece(Piece oldAttackPiece, Piece sacrificePiece) {
        Piece newAttackPiece = new Piece(oldAttackPiece.getName(), sacrificePiece.getCoordinates(), oldAttackPiece.getColorOfPiece(), false);
        pieces.remove(oldAttackPiece);
        pieces.remove(sacrificePiece);
        pieces.add(newAttackPiece);
        fillBusyCoordinatesList();
    }

    private void addTakenPiece(Piece piece) {

        if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK)) {

            if (piece.getName().equals("Pawn Black") ) {
                for (int i = 1; i < 9; i++) {
                    if (!isTakenPieceAtCoordinates(new Coordinate(9, i))) {
                        takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(9, i), ColorOfPiece.BLACK, false));
                        break;
                    }
                }
            }
            if (piece.getName().equals("Queen Black") ) {
                takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 4), ColorOfPiece.BLACK, false));
            }

            if (Objects.equals(piece.getName(), "Rook Black")) {
                if (!isTakenPieceAtCoordinates(new Coordinate(10, 1))) {
                    takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 1), ColorOfPiece.BLACK, false));
                } else {
                    takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 7), ColorOfPiece.BLACK, false));
                }
            }
            if (Objects.equals(piece.getName(), "Bishop Black")) {
                if (!isTakenPieceAtCoordinates(new Coordinate(10, 3))) {
                    takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 3), ColorOfPiece.BLACK, false));
                } else {
                    takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 5), ColorOfPiece.BLACK, false));
                }
            }
            if (Objects.equals(piece.getName(), "Knight Black")) {
                if (!isTakenPieceAtCoordinates(new Coordinate(10, 2))) {
                    takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 2), ColorOfPiece.BLACK, false));
                } else {
                    takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(10, 6), ColorOfPiece.BLACK, false));
                }
            }


        }

        if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE)) {

            if (Objects.equals(piece.getName(), "Pawn White")) {
                for (int i = 1; i < 9; i++) {
                    if (!isTakenPieceAtCoordinates(new Coordinate(11, i))) {
                        takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(11, i), ColorOfPiece.WHITE, false));
                        break;
                    }
                }
            }
            if (Objects.equals(piece.getName(), "Queen White")) {
                takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 4), ColorOfPiece.WHITE, false));
            }

            if (Objects.equals(piece.getName(), "Rook White")) {
                if (!isTakenPieceAtCoordinates(new Coordinate(12, 1))) {
                    takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 1), ColorOfPiece.WHITE, false));
                } else {
                    takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 7), ColorOfPiece.WHITE, false));
                }
            }
            if (Objects.equals(piece.getName(), "Bishop White")) {
                if (!isTakenPieceAtCoordinates(new Coordinate(12, 3))) {
                    takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 3), ColorOfPiece.WHITE, false));
                } else {
                    takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 5), ColorOfPiece.WHITE, false));
                }
            }
            if (Objects.equals(piece.getName(), "Knight White")) {
                if (!isTakenPieceAtCoordinates(new Coordinate(12, 2))) {
                    takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 2), ColorOfPiece.WHITE, false));
                } else {
                    takenPiecesWhite.add(new Piece(piece.getName(), new Coordinate(12, 6), ColorOfPiece.WHITE, false));
                }
            }


        }
    }

    public void makeBackEat(Piece oldAttackPiece, Piece sacrificePiece) {
        Piece newAttackPiece = new Piece(oldAttackPiece.getName(), sacrificePiece.getCoordinates(), oldAttackPiece.getColorOfPiece(), false);

        pieces.remove(newAttackPiece);
        pieces.add(oldAttackPiece);
        pieces.add(sacrificePiece);
        fillBusyCoordinatesList();
    }

    public void afterMove() {
        isCheckMate = false;
        check();
        isWhiteMoving = !isWhiteMoving;
        allowedMovesList.clear();
        setCheckText(isCheck);                              //пишет когда кому шах
        if (isCheck) {
            checkMate();
        }
        setCheckMateText(isCheckMate);
        setCountText();
        invalidate();
    }

    private static void setCountText() {
      /*  int scoreWhite = 0;
        int scoreBlack = 0;
        for (Piece piece : takenPiecesWhite) {
            if (piece.getName() == "Pawn White") {
                scoreBlack++;
            }
            if (piece.getName() == "Knight White" || piece.getName() == "Bishop White") {
                scoreBlack += 3;
            }
            if (piece.getName() == "Rook White") {
                scoreBlack += 5;
            }
            if (piece.getName() == "Queen White") {
                scoreBlack += 9;
            }
        }

        for (Piece piece : takenPiecesBlack) {
            if (piece.getName() == "Pawn Black") {
                scoreWhite++;
            }
            if (piece.getName() == "Knight Black" || piece.getName() == "Bishop Black") {
                scoreWhite += 3;
            }
            if (piece.getName() == "Rook Black") {
                scoreWhite += 5;
            }
            if (piece.getName() == "Queen Black") {
                scoreWhite += 9;
            }
        }
       MainActivity.countW.setText(String.format("+%d", scoreWhite - scoreBlack));
         if (scoreWhite - scoreBlack <= 0) {
            MainActivity.countW.setText("");
        }
        MainActivity.countB.setText(String.format("+%d", scoreBlack - scoreWhite));
        if (scoreBlack - scoreWhite <= 0) {
            MainActivity.countB.setText("");
        }

*/
    }

    private void check() {
        /*
        Очень важный метод - осуществялет проверку шаха в текущей позиции, после совершения хода.
        Вызывается после makeMove() и makeEat(), и делает следующее:
        1.Проход по всему списку pieces и вызов у каждой фигуры метода canThePieceMove,
        чтобы заполнить список ее возможных ходов.
        2. В списке возможных ходов каждой фигуры проверятся, может и она съесть короля белых или черных.
        3. Если да, то флаг isCheck становится true - ШАХ.
        4. После есть два варианта событий для текущей позиции фигур на доске:
        - Шаха нет, либо он есть, но не нашему королю - все нормально, делаем обычный ход, записываем историю и т.д
        - Шах есть, и он нашему королю - придется откатить move (eat) назад и удалить последнюю запись в истории.
        */
        isCheck = false;
        isCheckTheWhite = false;
        isCheckTheBlack = false;
        HashSet<Coordinate> LISTofMOVES = new HashSet<>();
        for (Piece piece : pieces) {
            canThePieceMove(piece, LISTofMOVES);
            for (Coordinate coor : LISTofMOVES) {
                Piece test = getPieceAtCoordinates(coor);
                if (test.getName().equals("King White")) {
                    isCheck = true;
                    isCheckTheWhite = true;
                    checkColor = ColorOfPiece.WHITE;
                }
                if (test.getName().equals("King Black")) {
                    isCheck = true;
                    isCheckTheBlack = true;
                    checkColor = ColorOfPiece.BLACK;
                }
            }
        }
    }

    public void checkMate() {
        /*
        1.Взять список всех фигур того цвета, чьему королю сделан шах.
        2.Взять каждую фигуру и применить к ней метод canThePieceMove(), наполнив список доступных ходов
        3.Брать каждый доступный ход и ставить туда фигуру методом move или eat.
        4.Проверять ситуацию на check своему королю и ОЧИЩАТЬ СПИСОК ХОДОВ
        5.Складывать все возможные ходы, позволяющие избежать шаха
        6.Если ходов = 0 - МАТ
        */
        isCheckMate = true;
        ArrayList<Piece> checkingPieces = new ArrayList<>();
        HashSet<Coordinate> LISTofMOVES = new HashSet<>();
//тут pieces превращается в поток данных, и фильруется по цвету. потом каждый элемент, прошедший фильтр методом forEach добавляется в коллекцию.
        if (!isWhiteMoving) {
            pieces.stream().filter(x -> x.getColorOfPiece().equals(ColorOfPiece.BLACK)).forEach(x -> checkingPieces.add(x));
        }
        if (isWhiteMoving) {
            pieces.stream().filter(x -> x.getColorOfPiece().equals(ColorOfPiece.WHITE)).forEach(x -> checkingPieces.add(x));
        }
        for (Piece piece : checkingPieces) {
            canThePieceMove(piece, LISTofMOVES);
            for (Coordinate coor : LISTofMOVES) {
                Piece test = getPieceAtCoordinates(coor);
                if (test.getName().equals("EmptyPlace")) {
                    Piece gotoPiece = new Piece(piece.getName(), coor, piece.getColorOfPiece(), piece.isFirstMove());
                    makeMove(piece, gotoPiece);
                    check();
                    if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                        isCheckMate = false;
                    }
                    if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                        isCheckMate = false;
                    }
                    makeBackMove(piece, gotoPiece);
                }
                if (!test.getName().equals("EmptyPlace")) {
                    eatThePiece(piece, test);
                    check();
                    if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                        isCheckMate = false;
                    }
                    if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                        isCheckMate = false;
                    }
                    makeBackEat(piece, test);
                }
            }
        }
    }


    public void writeWhoIsMove() {//ПИШЕТ КОГДА ЧЕЙ ХОД
        if (isWhiteMoving) {
            move=("Белые ходят");
        } else {
            move=("Черные ходят");
        }
    }

    private void setCheckMateText(boolean isCheckMate) {
        if (isCheckMate) {
            if (!isCheckTheWhite) {
                checkmate="Мат черным!";
            }
            if (isCheckTheWhite) {
                checkmate=("Мат белым!");
            }
        }
    }

    public void setCheckText(boolean isCheck) {
        if (isCheck) {

            if (!isCheckTheWhite) {
               checkmate=("Шах черным!");
            }
            if (isCheckTheWhite) {
                checkmate=("Шах белым!");
            }
        }
    }

    public static void makeDefaultPlacement() { //УСТАНАВЛИВАЕТ НАЧАЛЬНУЮ РАССТАНОВКУ ФИГУР
        pieces.clear();
        takenPiecesWhite.clear();
        takenPiecesBlack.clear();
        movesBuffer.clear();
        allowedMovesList.clear();
        history.clear();
        //isWhiteMoving = true;
        isCheck = false;
        isCheckMate = false;


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
        }

//        pieces.add(new Piece("Pawn White", new Coordinate(4, 4), ColorOfPiece.WHITE, false));
//         pieces.add(new Piece("Queen Black", new Coordinate(3, 3), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Pawn Black", new Coordinate(5, 4), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Pawn Black", new Coordinate(5, 5), ColorOfPiece.BLACK, false));
//         pieces.add(new Piece("Bishop White", new Coordinate(3, 4), ColorOfPiece.WHITE, false));
//         pieces.add(new Piece("Bishop Black", new Coordinate(2, 4), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Bishop White", new Coordinate(4, 2), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Queen White", new Coordinate(5, 2), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Knight Black", new Coordinate(8, 2), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("King White", new Coordinate(5, 1), ColorOfPiece.WHITE, true));

        //ПАРТИЯ
//        pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Rook Black", new Coordinate(1, 8), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Rook Black", new Coordinate(8, 8), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Knight Black", new Coordinate(2, 8), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Pawn Black", new Coordinate(1, 7), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Pawn Black", new Coordinate(2, 7), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Pawn Black", new Coordinate(3, 7), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Pawn White", new Coordinate(3, 2), ColorOfPiece.WHITE, true));
//        pieces.add(new Piece("Pawn Black", new Coordinate(7, 7), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Pawn Black", new Coordinate(8, 7), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Bishop Black", new Coordinate(6, 5), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("King White", new Coordinate(5, 1), ColorOfPiece.WHITE, true));
//        pieces.add(new Piece("Pawn White", new Coordinate(1, 2), ColorOfPiece.WHITE, true));
//        pieces.add(new Piece("Pawn White", new Coordinate(7, 2), ColorOfPiece.WHITE, true));
//        pieces.add(new Piece("Pawn White", new Coordinate(4, 4), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Queen Black", new Coordinate(3, 3), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Pawn Black", new Coordinate(5, 4), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Pawn Black", new Coordinate(5, 5), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Bishop White", new Coordinate(3, 4), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Bishop Black", new Coordinate(2, 4), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Bishop White", new Coordinate(4, 2), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Queen White", new Coordinate(5, 2), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Knight Black", new Coordinate(8, 2), ColorOfPiece.BLACK, false));
//        pieces.add(new Piece("Pawn Black", new Coordinate(6, 7), ColorOfPiece.BLACK, true));
//        pieces.add(new Piece("Pawn White", new Coordinate(6, 3), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Knight White", new Coordinate(6, 2), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Rook White", new Coordinate(6, 1), ColorOfPiece.WHITE, false));
//        pieces.add(new Piece("Rook White", new Coordinate(3, 1), ColorOfPiece.WHITE, false));


      /*  takenPieces.add(new Piece("Queen Black", new Coordinate(10, 4), ColorOfPiece.BLACK, false));
        takenPieces.add(new Piece("Rook Black", new Coordinate(10, 1), ColorOfPiece.BLACK, false));
        takenPieces.add(new Piece("Rook Black", new Coordinate(10, 7), ColorOfPiece.BLACK, false));
        takenPieces.add(new Piece("Knight Black", new Coordinate(10, 2), ColorOfPiece.BLACK, false));
        takenPieces.add(new Piece("Knight Black", new Coordinate(10, 6), ColorOfPiece.BLACK, false));
        takenPieces.add(new Piece("Bishop Black", new Coordinate(10, 3), ColorOfPiece.BLACK, false));
        takenPieces.add(new Piece("Bishop Black", new Coordinate(10, 5), ColorOfPiece.BLACK, false));

        takenPieces.add(new Piece("Queen White", new Coordinate(12, 4), ColorOfPiece.WHITE, false));
        takenPieces.add(new Piece("Rook White", new Coordinate(12, 1), ColorOfPiece.WHITE, false));
        takenPieces.add(new Piece("Rook White", new Coordinate(12, 7), ColorOfPiece.WHITE, false));
        takenPieces.add(new Piece("Knight White", new Coordinate(12, 2), ColorOfPiece.WHITE, false));
        takenPieces.add(new Piece("Knight White", new Coordinate(12, 6), ColorOfPiece.WHITE, false));
        takenPieces.add(new Piece("Bishop White", new Coordinate(12, 3), ColorOfPiece.WHITE, false));
        takenPieces.add(new Piece("Bishop White", new Coordinate(12, 5), ColorOfPiece.WHITE, false));
        for (int i = 1; i <= 8; i++) {
            takenPieces.add(new Piece("Pawn Black", new Coordinate(9, i), ColorOfPiece.BLACK, false));
            takenPieces.add(new Piece("Pawn White", new Coordinate(11, i), ColorOfPiece.WHITE, false));
        }*/


        fillBusyCoordinatesList();
    }

    private static void fillBusyCoordinatesList() {
        busyCoordinates.clear();
        for (Piece piece : pieces) {
            busyCoordinates.add(piece.getCoordinates());
        }
    }

    private boolean isPieceAtCoordinates(Coordinate coordinate) {     //проверяет стоит ли фигура на указанном по координатам квадрате
        return busyCoordinates.contains(coordinate);
    }

    private boolean isTakenPieceAtCoordinates(Coordinate coordinate) {     //проверяет стоит ли фигура на указанном по координатам квадрате
        for (Piece piece : takenPiecesWhite) {
            if (piece.getCoordinates().equals(coordinate)) {
                return true;
            }
        }
        for (Piece piece : takenPiecesBlack) {
            if (piece.getCoordinates().equals(coordinate)) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом
        drawChessBoard(canvas);                             //отрисовка доски
        drawMarkup(canvas);
        drawPieces(canvas);                                 //отрисовка фигур
        lightAllowedMoves(canvas, allowedMovesList);     //отрисовка доступных ходов
        writeWhoIsMove();                                   //пишет чей ход
        drawTakenWhitePieces(canvas);
        drawTakenBlackPieces(canvas);
        drawTakenPieces(canvas, takenPiecesWhite);
        drawTakenPieces(canvas, takenPiecesBlack);

    }



    private void drawTakenWhitePieces(Canvas canvas) {

        Paint paint = new Paint();
        initialisationOfXY();

        float startX = defX + squareSide - 60;
        float startY = defY - squareSide;
        float finishX = defX + chessBoardSize - squareSide - 80;
        float finishY = startY + 30;


        paint.setColor(Color.RED);

        // paint.setAlpha(80);
        //canvas.drawRect(defX, defY-(squareSide*2), defX+chessBoardSize, defY, paint);
        paint.setColor(Color.GRAY);
        // canvas.drawRect(defX+squareSide, defY-(squareSide*2), defX+chessBoardSize-squareSide, defY-squareSide, paint);

        // defY = 455;
        int gap = 3;
        int gapOlderPieces = 10;
        int pawnGap = 18;

        float olderPiecesX = startX + 20;
        float sqSide = squareSide - 20 * 2;
        float olderPiecesY = startY - sqSide - 20;

        float pawnPiecesX = startX + 50;
        float pawnPiecesY = startY - sqSide + 15;
        float sqSidePawn = squareSide - 25 * 2.6f;
        float pawnGapX = pawnPiecesX + sqSidePawn;
        float pawnGapY = pawnPiecesY;

        paint.setShader(new LinearGradient(startX, startY, defX + chessBoardSize - squareSide, defY, Color.GRAY, Color.DKGRAY, Shader.TileMode.CLAMP));
        canvas.drawRect(startX, startY, finishX, finishY, paint);

        Paint paint1 = new Paint();
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(4);
        paint1.setColor(Color.BLACK);
        canvas.drawRect(startX - gap, startY, finishX + gap, startY + 30, paint1);

        canvas.drawLine(finishX + gap, startY, (finishX + gap) + 30, startY - 30, paint1);
        canvas.drawLine(finishX + gap, startY + 30, (finishX + gap) + 30, startY, paint1);
        canvas.drawLine(startX - gap, startY, (startX - gap) + 30, startY - 30, paint1);


        Paint paint4 = new Paint();
        Paint paint5 = new Paint();

        paint5.setColor(Color.BLUE);
        paint4.setColor(Color.GREEN);

        paint4.setAlpha(80);
        paint5.setAlpha(80);


        for (int i = 0; i < 8; i++) {
            RectF rectF = new RectF(pawnPiecesX + sqSidePawn * i + pawnGap * i, pawnPiecesY, pawnPiecesX + sqSidePawn * (i + 1) + pawnGap * i, pawnPiecesY + sqSidePawn);
            // canvas.drawRect(rectF, paint4);
            takenPiecesRects.put(new Coordinate(11, i + 1), rectF);
            // canvas.drawRect(pawnPiecesX + sqSidePawn * (i+1) + pawnGap*i, pawnGapY, pawnPiecesX + sqSidePawn * (i+1) + pawnGap  *(i+1), pawnPiecesY + sqSidePawn, paint5);
        }

        for (int i = 0; i < 7; i++) {
            RectF rectF = new RectF(olderPiecesX + (sqSide * i) + gapOlderPieces * i, olderPiecesY, olderPiecesX + sqSide * (i + 1) + gapOlderPieces * i, olderPiecesY + sqSide);
            // canvas.drawRect(rectF, paint3);
            takenPiecesRects.put(new Coordinate(12, i + 1), rectF);
            // canvas.drawRect(olderPiecesX + (sqSide * (i + 1)) + gapOlderPieces * i, olderPiecesY, olderPiecesX + (sqSide * (i + 1)) + gapOlderPieces * (i + 1), olderPiecesY + sqSide, paint5);
        }


    }

    private void drawTakenBlackPieces(Canvas canvas) {

        Paint paint = new Paint();
        initialisationOfXY();

        float startX = defX + squareSide - 60;
        float startY = defY - squareSide * 2;
        float finishX = defX + chessBoardSize - squareSide - 80;
        float finishY = startY + 30;


        paint.setColor(Color.RED);
        // paint.setAlpha(80);
        //canvas.drawRect(defX, defY-(squareSide*2), defX+chessBoardSize, defY, paint);
        paint.setColor(Color.GRAY);
        // canvas.drawRect(defX+squareSide, defY-(squareSide*2), defX+chessBoardSize-squareSide, defY-squareSide, paint);

        // defY = 455;
        int gap = 3;
        int gapOlderPieces = 10;
        int pawnGap = 18;

        float olderPiecesX = startX + 20;
        float sqSide = squareSide - 20 * 2;
        float olderPiecesY = startY - sqSide - 20;

        float pawnPiecesX = startX + 50;
        float pawnPiecesY = startY - sqSide + 15;
        float sqSidePawn = squareSide - 25 * 2.6f;
        float pawnGapX = pawnPiecesX + sqSidePawn;
        float pawnGapY = pawnPiecesY;

        paint.setShader(new LinearGradient(startX, startY, defX + chessBoardSize - squareSide, defY, Color.GRAY, Color.DKGRAY, Shader.TileMode.CLAMP));
        canvas.drawRect(startX, startY, finishX, finishY, paint);

        Paint paint1 = new Paint();
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(4);
        paint1.setColor(Color.BLACK);
        canvas.drawRect(startX - gap, startY, finishX + gap, startY + 30, paint1);

        canvas.drawLine(finishX + gap, startY, (finishX + gap) + 30, startY - 30, paint1);
        canvas.drawLine(finishX + gap, startY + 30, (finishX + gap) + 30, startY, paint1);
        canvas.drawLine(startX - gap, startY, (startX - gap) + 30, startY - 30, paint1);


        Paint paint4 = new Paint();
        Paint paint5 = new Paint();

        paint5.setColor(Color.BLUE);
        paint4.setColor(Color.GREEN);

        paint4.setAlpha(80);
        paint5.setAlpha(80);


        for (int i = 0; i < 8; i++) {
            RectF rectF = new RectF(pawnPiecesX + sqSidePawn * i + pawnGap * i, pawnPiecesY, pawnPiecesX + sqSidePawn * (i + 1) + pawnGap * i, pawnPiecesY + sqSidePawn);
            // canvas.drawRect(rectF, paint4);
            takenPiecesRects.put(new Coordinate(9, i + 1), rectF);
            // canvas.drawRect(pawnPiecesX + sqSidePawn * (i+1) + pawnGap*i, pawnGapY, pawnPiecesX + sqSidePawn * (i+1) + pawnGap  *(i+1), pawnPiecesY + sqSidePawn, paint5);
        }

        for (int i = 0; i < 7; i++) {
            RectF rectF = new RectF(olderPiecesX + (sqSide * i) + gapOlderPieces * i, olderPiecesY, olderPiecesX + sqSide * (i + 1) + gapOlderPieces * i, olderPiecesY + sqSide);
            // canvas.drawRect(rectF, paint3);
            takenPiecesRects.put(new Coordinate(10, i + 1), rectF);
            // canvas.drawRect(olderPiecesX + (sqSide * (i + 1)) + gapOlderPieces * i, olderPiecesY, olderPiecesX + (sqSide * (i + 1)) + gapOlderPieces * (i + 1), olderPiecesY + sqSide, paint5);
        }


    }

    public void initialisationOfXY() { //т.к. методы getWidth() и getHeight() (View) не хотят возвращать значения при начальной инициализации, придется сделать ее в отдельном методе
        chessBoardSize = Math.min(getWidth(), getHeight()) * scaleFactor;
        squareSide = (int) (chessBoardSize / 8);
        defX = (getWidth() - chessBoardSize) / 2f;
        defY = (getHeight() - chessBoardSize) / 2.5f;

    }

    private void drawPieces(Canvas canvas) {            //рисует все фигуры из листа pieces
        for (Piece i : pieces) {
            drawPieceAt(canvas, listOfPiecesAndPNG.get(i.getName()), reduceThePiece(squares.get(i.getCoordinates())));
        }
    }

    private void drawTakenPieces(Canvas canvas, ArrayList<Piece> pieces) {            //рисует все взятые фигуры из листа pieces
        for (Piece i : pieces) {
            drawPieceAt(canvas, listOfTakenPiecesAndPNG.get(i.getName()), takenPiecesRects.get(i.getCoordinates()));
        }
    }

    private RectF reduceThePiece(RectF square) {              //уменьшает входящий квадрат на размер indent
        return new RectF(square.left + indent, square.top + indent, square.right - indent, square.bottom - indent);
    }

    private RectF reduceThePiece(RectF square, boolean flag) {              //уменьшает входящий квадрат на размер indent
        return new RectF(square.left + indent, square.top + indent, square.right, square.bottom);
    }

    private void drawPieceAt(Canvas canvas, Bitmap bitmap, RectF rect) {        //рисует фигуру по указанному квадрату
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, null, rect, paint);
    }

    public void drawChessBoard(Canvas canvas) {
        Paint paint = new Paint();
        initialisationOfXY();

        boolean white_black_flag = true;
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                if (white_black_flag) {
                    paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));

                } else {
                    paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));

                }
                white_black_flag = !white_black_flag;
                canvas.drawRect(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide), paint);

                if (isInvertedBoard) {
                    squares.put(new Coordinate(8 - i, j + 1), new RectF(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide)));
                } else {

                    squares.put(new Coordinate(i + 1, 8 - j), new RectF(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide)));
                }
            }
            defY += squareSide;
            white_black_flag = !white_black_flag;
        }
    }

    private void drawMarkup(Canvas canvas) {
        Paint paint = new Paint();
        boolean white_black_flag = true;
        String[] text = new String[]{"A","B","C","D","E","F","G","H"};
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(28);
        for (int i = 0; i < 8; i++) {
            if (white_black_flag) {
                paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));
            } else {
                paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));
            }
            RectF rect = squares.get(new Coordinate(i+1,1));
            canvas.drawText(text[i], rect.left+squareSide/16, rect.bottom-squareSide/16,paint);
            white_black_flag = !white_black_flag;
        }
        for (int i = 0; i < 8; i++) {
            if (white_black_flag) {
                paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));
            } else {
                paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));
            }
            RectF rect = squares.get(new Coordinate(1,i+1));
            canvas.drawText(String.valueOf(i+1), rect.left+squareSide/16, rect.top+squareSide/4,paint);
            white_black_flag = !white_black_flag;
        }
    }

    private Piece getPieceAtCoordinates(Coordinate coordinate) {             //возвращает фигуру по указанным координатам
        for (Piece piece : pieces) {
            if (piece.getCoordinates().equals(coordinate)) {
                return piece;
            }
        }
        return testPiece;
    }

    private void lightAllowedMoves(Canvas canvas, HashSet<Coordinate> coordinates) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(colors.get("Цвет выделения свободного хода")));        //цвет выделения свободных ходов
        paint.setAlpha(200);
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius0fFreeMovePoint, paint);
        }
    }

    private boolean canMoveThere(Coordinate coordinate) { //проверяет, может ли фигура пойти на указанные координаты
        return allowedMovesList.contains(coordinate);
    }

    private boolean checkBordersOfView(float x, float y) {           //проверка не выходит ли указатель за рамки view при перемещении фигуры
        return x < GameActivity.board.getLeft() || x > GameActivity.board.getRight() || y < GameActivity.board.getTop() || y > GameActivity.board.getBottom();
    }

    private Coordinate invertedCoordinates(float coorX, float coorY) { //возвращает координаты, инвертированные или нет, в зав. от настроек
        initialisationOfXY();
        if (isInvertedBoard) {
            /*ИНВЕРТИРОВАННАЯ */
            return new Coordinate((int) (9 - (coorX - defX) / squareSide), 1 + ((int) (coorY - defY) / squareSide));
        } else {
            /* ОБЫЧНАЯ */
            return new Coordinate(1 + ((int) (coorX - defX) / squareSide), (int) (9 - (coorY - defY) / squareSide));
        }
    }

    private void multyMove(Piece piece, HashSet<Coordinate> lightedSquares) {
        HashSet<Coordinate> LISTofMOVES = new HashSet<>();
        LISTofMOVES.addAll(lightedSquares);
        lightedSquares.clear();

        for (Coordinate coor : LISTofMOVES) {
            Piece test = getPieceAtCoordinates(coor);
            if (test.getName().equals("EmptyPlace")) {
                Piece gotoPiece = new Piece(piece.getName(), coor, piece.getColorOfPiece(), piece.isFirstMove());
                makeMove(piece, gotoPiece);
                check();
                if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                    lightedSquares.add(coor);
                }
                if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                    lightedSquares.add(coor);
                }
                makeBackMove(piece, gotoPiece);
            }
            if (!test.getName().equals("EmptyPlace")) {
                eatThePiece(piece, test);
                check();
                if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                    lightedSquares.add(coor);
                }
                if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                    lightedSquares.add(coor);
                }
                makeBackEat(piece, test);
            }
        }
    }

    //заносит в указанный лист те клетки, куда может пойти указанная фигура, в зав. от ее имени
    private void canThePieceMove(Piece piece, HashSet<Coordinate> list) {
        Coordinate coordinates = piece.getCoordinates();

        if (!list.isEmpty()) {
            list.clear();
        }

        if (piece.getName().equals("King White") || piece.getName().equals("King Black")) {
            for (int j = 1; j >= -1; j--) {
                for (int i = 1; i >= -1; i--) {
                    list.add(new Coordinate(coordinates.getLetter() - j, coordinates.getNumber() - i));
                }
            }
        }
        if (piece.getName().equals("Rook White") || piece.getName().equals("Rook Black")) {
            verticalAndHorizontalPassage(coordinates, list);
        }
        if (piece.getName().equals("Bishop White") || piece.getName().equals("Bishop Black")) {
            diagonalPassage(coordinates, list);
        }
        if (piece.getName().equals("Queen White") || piece.getName().equals("Queen Black")) {
            diagonalPassage(coordinates, list);
            verticalAndHorizontalPassage(coordinates, list);
        }

        if (piece.getName().equals("Pawn White")) {
            if (piece.isFirstMove()) {
                if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 1));
                }
                if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 2)) && !isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 2));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() + 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() + 1));
                }
            }
            if (!piece.isFirstMove()) {
                if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter(), coordinates.getNumber() + 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() + 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() + 1))) {
                    list.add(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() + 1));
                }
            }
        }
        if (piece.getName().equals("Pawn Black")) {
            if (piece.isFirstMove()) {
                if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 1));
                }
                if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 2)) && !isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 2));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() - 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() - 1));
                }
            }
            if (!piece.isFirstMove()) {
                if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter(), coordinates.getNumber() - 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() - 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() - 1))) {
                    list.add(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() - 1));
                }
            }
        }
        if (piece.getName().equals("Knight White") || piece.getName().equals("Knight Black")) {

            list.add(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() + 2));
            list.add(new Coordinate(coordinates.getLetter() + 1, coordinates.getNumber() - 2));
            list.add(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() + 2));
            list.add(new Coordinate(coordinates.getLetter() - 1, coordinates.getNumber() - 2));

            list.add(new Coordinate(coordinates.getLetter() + 2, coordinates.getNumber() - 1));
            list.add(new Coordinate(coordinates.getLetter() + 2, coordinates.getNumber() + 1));
            list.add(new Coordinate(coordinates.getLetter() - 2, coordinates.getNumber() + 1));
            list.add(new Coordinate(coordinates.getLetter() - 2, coordinates.getNumber() - 1));

        }
        list.removeIf(i -> getPieceAtCoordinates(i).getColorOfPiece().equals(piece.getColorOfPiece()));
        list.removeIf(coor -> coor.getLetter() > 8
                || coor.getLetter() < 1
                || coor.getNumber() > 8
                || coor.getNumber() < 1);

    }

    private void verticalAndHorizontalPassage(Coordinate coordinates, HashSet<Coordinate> list) {     //ищет доступные ходы для фигуры по вертикали и горизонтали

        for (int i = coordinates.getNumber() + 1; i <= 8; i++) {          //проход по вертикали вверх
            if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), i))) {
                list.add(new Coordinate(coordinates.getLetter(), i));
            } else {
                list.add(new Coordinate(coordinates.getLetter(), i));
                break;
            }
        }
        for (int i = coordinates.getNumber() - 1; i >= 1; i--) {          //проход по вертикали вниз
            if (!isPieceAtCoordinates(new Coordinate(coordinates.getLetter(), i))) {
                list.add(new Coordinate(coordinates.getLetter(), i));
            } else {
                list.add(new Coordinate(coordinates.getLetter(), i));
                break;
            }
        }

        for (int i = coordinates.getLetter() + 1; i <= 8; i++) {          //проход по горизонтали вверх
            if (!isPieceAtCoordinates(new Coordinate(i, coordinates.getNumber()))) {
                list.add(new Coordinate(i, coordinates.getNumber()));
            } else {
                list.add(new Coordinate(i, coordinates.getNumber()));
                break;
            }
        }

        for (int i = coordinates.getLetter() - 1; i >= 1; i--) {          //проход по горизонтали вниз
            if (!isPieceAtCoordinates(new Coordinate(i, coordinates.getNumber()))) {
                list.add(new Coordinate(i, coordinates.getNumber()));
            } else {
                list.add(new Coordinate(i, coordinates.getNumber()));
                break;
            }
        }
    }

    private void diagonalPassage(Coordinate coordinates, HashSet<Coordinate> list) { //ищет доступные ходы для фигуры по диагоналям
        for (int i = coordinates.getLetter() + 1, j = coordinates.getNumber() + 1; i <= 8 && j <= 8; i++, j++) {  //проход по правой диагонали вверх
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                list.add(new Coordinate(i, j));
            } else {
                list.add(new Coordinate(i, j));
                break;
            }
        }
        for (int i = coordinates.getLetter() - 1, j = coordinates.getNumber() - 1; i >= 1 && j >= 1; i--, j--) {  //проход по правой диагонали вниз
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                list.add(new Coordinate(i, j));
            } else {
                list.add(new Coordinate(i, j));
                break;
            }
        }
        for (int i = coordinates.getLetter() - 1, j = coordinates.getNumber() + 1; i >= 1 && j <= 8; i--, j++) {  //проход по левой диагонали вверх
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                list.add(new Coordinate(i, j));
            } else {
                list.add(new Coordinate(i, j));
                break;
            }
        }
        for (int i = coordinates.getLetter() + 1, j = coordinates.getNumber() - 1; i <= 8 && j >= 1; i++, j--) {  //проход по левой диагонали вниз
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                list.add(new Coordinate(i, j));
            } else {
                list.add(new Coordinate(i, j));
                break;
            }
        }
    }
}
