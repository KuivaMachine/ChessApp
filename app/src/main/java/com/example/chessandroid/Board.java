package com.example.chessandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.chessandroid.Pieces.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;

public class Board extends View {

    Piece defaultPiece = new Piece("Test", new Coordinate(8, 8), ColorWB.WHITE); //тестовая фигура, всплывает если что то не так
    Paint paint = new Paint();


    final private String TAG = "MainActivity";
    boolean isSquareEmpty = true;
    boolean isTouchMoving = false;

    //эти 3 значения переопределяются в onDraw
    int defX = 0;
    int defY = 0;
    int squareSide = 120;               //размер квадрата

    static ArrayList<Piece> pieces = new ArrayList<>();
    static LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    HashMap<Piece, Bitmap> listOfPiecesAndPNG = new HashMap<>();
    HashSet<Coordinate> lightedSquares = new HashSet<>();

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makePNGList();


        pieces.add(new Piece("King White", new Coordinate(4, 5), ColorWB.WHITE));
        pieces.add(new Piece("King Black", new Coordinate(4, 6), ColorWB.BLACK));
        pieces.add(new Piece("Queen White", new Coordinate(5, 1), ColorWB.WHITE));

    }


    public void makePNGList() {
        listOfPiecesAndPNG.put(new Piece("King White", new Coordinate(4, 5), ColorWB.WHITE), BitmapFactory.decodeResource(getResources(), R.drawable.king_white));
        listOfPiecesAndPNG.put(new Piece("King Black", new Coordinate(4, 6), ColorWB.BLACK), BitmapFactory.decodeResource(getResources(), R.drawable.king_black));
        listOfPiecesAndPNG.put(new Piece("Queen White", new Coordinate(5, 1), ColorWB.WHITE), BitmapFactory.decodeResource(getResources(), R.drawable.queen_white));
        listOfPiecesAndPNG.put(new Piece("Test", new Coordinate(8, 8), ColorWB.WHITE), BitmapFactory.decodeResource(getResources(), R.drawable.test_piece));

    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        defX = 0;
        defY = 0;
        squareSide = 120;
        //   canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом

        drawChessBoard(canvas);
        lightTheSquares(canvas, lightedSquares);
        lightedSquares.clear();
        drawPieces(canvas);
    }

    private void drawPieces(Canvas canvas) {            //рисует все фигуры из листа pieces
        for (Piece i : pieces) {
            drawPieceAt(canvas, listOfPiecesAndPNG.get(i), squareToDown(Objects.requireNonNull(squares.get(i.getCoordinates()))));
        }
    }

    private RectF squareToDown(RectF square) {              //уменьшает входящий квадрат на размер indent
        float indent = 8;                   //размер отступа внутри клетки до фигуры
        return new RectF(square.left + indent, square.top + indent, square.right - indent, square.bottom - indent);
    }

    private void drawPieceAt(Canvas canvas, Bitmap bitmap, RectF rect) {        //рисует фигуру по указанному квадрату
        canvas.drawBitmap(bitmap, null, rect, paint);
    }

    public void drawChessBoard(Canvas canvas) {
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(20);
        boolean colorWB = true;
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                if (colorWB) {
                    paint.setColor(Color.GRAY);
                } else {
                    paint.setColor(Color.GREEN);
                }
                colorWB = !colorWB;
                canvas.drawRect(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide), paint);
                squares.put(new Coordinate(i + 1, 8 - j), new RectF(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide)));
            }
            defY += squareSide;
            colorWB = !colorWB;
        }
        defY = 0;
    }

    private boolean isPieceAtCoordinates(Coordinate coordinate) {       //проверяет стоит ли на указанном по координатам квадрате фигура
        for (Piece i : pieces) {
            if (i.getCoordinates().equals(coordinate)) {
                return true;
            }
        }
        return false;
    }

    private Piece getPieceAtCoordinates(Coordinate coordinate) {             //возвращает фигуру по указанным координатам
        for (Piece i : pieces) {
            if (i.getCoordinates().equals(coordinate)) {
                return i;
            }
        }
        return null;
    }

    private void lightTheSquares(Canvas canvas, HashSet<Coordinate> coordinates) {
        paint.setColor(Color.RED);
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            canvas.drawRect(rect, paint);
        }

    }

    private boolean checkBordersOfView(float x, float y) {           //проверка не выходит ли указатель за рамки view при перемещении фигуры
        return x < MainActivity.board.getLeft() || x > MainActivity.board.getRight() || y < MainActivity.board.getTop() || y > MainActivity.board.getBottom();
    }

    private void makeMove(Piece oldPiece, Piece newPiece) {
       /* if (oldPiece.getCoordinates().letter > 6
                || oldPiece.getCoordinates().letter < 2
                || oldPiece.getCoordinates().number > 6
                || oldPiece.getCoordinates().number <= 2) {
            pieces.add(oldPiece);
            pieces.remove(oldPiece);

        } else {
            pieces.add(newPiece);
            pieces.remove(oldPiece);

        }*/
        MainActivity.coor.setText(String.format("%s%s", newPiece.getCoordinates().letter, newPiece.getCoordinates().number));
        invalidate();

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Coordinate currentCoor = new Coordinate(1 + ((int) (event.getX() - defX) / squareSide), (int) (9 - (event.getY() - defY) / squareSide));
            if (isPieceAtCoordinates(currentCoor)) {
                MainActivity.coor.setText(String.format("%s%s", 1 + ((int) (event.getX() - defX) / squareSide), (int) (9 - (event.getY() - defY) / squareSide)));
                defaultPiece = getPieceAtCoordinates(currentCoor);

                //   lightAnArea(defaultPiece.getCoordinates());
                Log.d(TAG, "took");
            } else {
                isSquareEmpty = false;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
             lightedSquares.clear();
              invalidate();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!checkBordersOfView(event.getX(), event.getY())) {
                Coordinate currentCoor = new Coordinate(1 + ((int) (event.getX() - defX) / squareSide), (int) (9 - (event.getY() - defY) / squareSide));

                if (!isPieceAtCoordinates(currentCoor) && isSquareEmpty) {
                    Piece newPiece = defaultPiece;
                   // newPiece.setCoordinates(currentCoor);
                    //makeMove(defaultPiece, newPiece);
                    //invalidate();
                    Log.d(TAG, "moved" + " " + defaultPiece.getName());
                } else {
                    Log.d(TAG, "square is busy");
                }
            }
            isSquareEmpty = true;
        }

        return true;
    }


    private void lightAnArea(Coordinate coordinates) {
        for (int j = 1; j >= -1; j--) {
            for (int i = 1; i >= -1; i--) {
                lightedSquares.add(new Coordinate(coordinates.letter - j, coordinates.number - i));
            }
        }

        lightedSquares.removeIf(this::isPieceAtCoordinates);
        lightedSquares.removeIf(coor -> coor.letter > 8
                || coor.letter < 1
                || coor.number > 8
                || coor.number < 1);

        invalidate();

    }


}
