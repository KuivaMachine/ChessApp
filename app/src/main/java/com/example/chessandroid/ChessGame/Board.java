package com.example.chessandroid.ChessGame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import androidx.annotation.NonNull;

import com.example.chessandroid.classes.ChessGame;
import com.example.chessandroid.enums.ColorOfPiece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;


public class Board extends View {
    ChessGame chessGame = new ChessGame();

    Piece bufferPiece = new Piece();

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


    //МАССИВЫ И ЛИСТЫ

    //  static ArrayList<Piece> takenPiecesWhite = new ArrayList<>();
    //  static ArrayList<Piece> takenPiecesBlack = new ArrayList<>();
    private final LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    private final LinkedHashMap<Coordinate, RectF> takenPiecesRects = new LinkedHashMap<>();
    private final HashSet<Coordinate> possibleMovesList = new HashSet<>();
    private final HashMap<String, Bitmap> listOfPiecesAndPNG = Bitmaps.listOfPiecesAndPNG;
    private final HashMap<String, Bitmap> listOfTakenPiecesAndPNG = Bitmaps.listOfTakenPiecesAndPNG;
    private final HashMap<String, String> colors = Bitmaps.colors;

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        new Bitmaps(this.getContext());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!checkBordersOfView(event.getX(), event.getY())) {
                Coordinate currentCoordinate = getCoordinatesByXY(event.getX(), event.getY());
                RectF rect = squares.get(currentCoordinate);
                Log.d(TAG, String.valueOf(rect));
                if (getPieceAtCoordinates(currentCoordinate, chessGame.pieces).getColorOfPiece() == ColorOfPiece.WHITE && chessGame.isWhiteMoving) {
                    bufferPiece = getPieceAtCoordinates(currentCoordinate, chessGame.pieces);
                    chessGame.fillListOfPossibleMoves(bufferPiece, possibleMovesList);
                    chessGame.removeDangerMoves(bufferPiece, possibleMovesList);
                }
                if (getPieceAtCoordinates(currentCoordinate, chessGame.pieces).getColorOfPiece() == ColorOfPiece.BLACK && !chessGame.isWhiteMoving) {
                    bufferPiece = getPieceAtCoordinates(currentCoordinate, chessGame.pieces);
                    chessGame.fillListOfPossibleMoves(bufferPiece, possibleMovesList);
                    chessGame.removeDangerMoves(bufferPiece, possibleMovesList);
                }
                if (chessGame.isPieceAtCoordinates(currentCoordinate) && canMoveThere(currentCoordinate)) {
                    Piece sacrificePiece = getPieceAtCoordinates(currentCoordinate, chessGame.pieces);
                    chessGame.eatThePiece(bufferPiece, sacrificePiece);
                    chessGame.addTakenPiece(sacrificePiece);

                    History history = new History(bufferPiece, "eat", sacrificePiece);
                    chessGame.history.add(history);
                    GameActivity.sendMove(history);
                    possibleMovesList.clear();

                }
                if (!chessGame.isPieceAtCoordinates(currentCoordinate) && canMoveThere(currentCoordinate)) {
                    Piece gotoPiece = new Piece(bufferPiece.getName(), currentCoordinate, bufferPiece.getColorOfPiece(), false);
                    chessGame.makeMove(bufferPiece, gotoPiece);
                    History history = new History(bufferPiece, "move", gotoPiece);
                    chessGame.history.add(history);
                    GameActivity.sendMove(history);
                    possibleMovesList.clear();
                }
                if (!chessGame.isPieceAtCoordinates(currentCoordinate)) {
                    possibleMovesList.clear();
                }
                //Log.d(TAG, String.valueOf(chessGame.isWhiteMoving));
                invalidate();
            }
        }
        return true;
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом
        drawChessBoard(canvas);                             //отрисовка доски
        drawMarkup(canvas);
        drawPieces(canvas);                                 //отрисовка фигур
        drawPossibleMoves(canvas, possibleMovesList);     //отрисовка доступных ходов

        /*drawTakenWhitePieces(canvas);
        drawTakenBlackPieces(canvas);
        drawTakenPieces(canvas, takenPiecesWhite);
        drawTakenPieces(canvas, takenPiecesBlack);*/

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
        for (Piece i : chessGame.pieces) {
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

                if (GameActivity.isInvertedBoard) {
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
        String[] text = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(28);
        for (int i = 0; i < 8; i++) {
            if (white_black_flag) {
                paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));
            } else {
                paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));
            }
            RectF rect = squares.get(new Coordinate(i + 1, 1));
            canvas.drawText(text[i], rect.left + squareSide / 16, rect.bottom - squareSide / 16, paint);
            white_black_flag = !white_black_flag;
        }
        for (int i = 0; i < 8; i++) {
            if (white_black_flag) {
                paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));
            } else {
                paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));
            }
            RectF rect = squares.get(new Coordinate(1, i + 1));
            canvas.drawText(String.valueOf(i + 1), rect.left + squareSide / 16, rect.top + squareSide / 4, paint);
            white_black_flag = !white_black_flag;
        }
    }

    private Piece getPieceAtCoordinates(Coordinate coordinate, ArrayList<Piece> pieces) {             //возвращает фигуру по указанным координатам
        for (Piece piece : pieces) {
            if (piece.getCoordinates().equals(coordinate)) {
                return piece;
            }
        }
        return new Piece("EmptyPlace", new Coordinate(8, 8), ColorOfPiece.TEST, false);
    }

    private void drawPossibleMoves(Canvas canvas, HashSet<Coordinate> coordinates) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor(colors.get("Цвет выделения свободного хода")));        //цвет выделения свободных ходов
        paint.setAlpha(200);
        Log.d(TAG, "3 = " + squares.size());
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            if(rect==null){
                Log.d(TAG, "rect=null = " + squares.size());
            }else{
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius0fFreeMovePoint, paint);}
        }
    }

    private boolean canMoveThere(Coordinate coordinate) { //проверяет, может ли фигура пойти на указанные координаты
        return possibleMovesList.contains(coordinate);
    }

    private boolean checkBordersOfView(float x, float y) {           //проверка не выходит ли указатель за рамки view при перемещении фигуры
        return x < GameActivity.board.getLeft() || x > GameActivity.board.getRight() || y < GameActivity.board.getTop() || y > GameActivity.board.getBottom();
    }

    private Coordinate getCoordinatesByXY(float coorX, float coorY) { //возвращает координаты, инвертированные или нет, в зав. от настроек
        initialisationOfXY();
        if (GameActivity.isInvertedBoard) {
            /*ИНВЕРТИРОВАННАЯ */
            return new Coordinate((int) (9 - (coorX - defX) / squareSide), 1 + ((int) (coorY - defY) / squareSide));
        } else {
            /* ОБЫЧНАЯ */
            return new Coordinate(1 + ((int) (coorX - defX) / squareSide), (int) (9 - (coorY - defY) / squareSide));
        }
    }


}
