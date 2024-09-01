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

public class Board extends View {

    Piece defaultPiece = new Piece("Test", new Coordinate(8, 8), ColorWB.WHITE); //тестовая фигура, всплывает если что то не так
    Paint paint = new Paint();


    final private String TAG = "MainActivity";
    int defX = 0;                   //координата Х для начала отсчета доски
    int defY = 0;                   //координата У для начала отсчета доски
    int squareSide = 120;           //размер квадрата
    float radius0fFreeMovePoint = 15;    //радиус точки, указывающей на доступные ходы
    float indent = 5;                   //размер отступа внутри клетки до фигуры


    static ArrayList<Piece> pieces = new ArrayList<>();
    static LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    HashMap<String, Bitmap> listOfPiecesAndPNG = new HashMap<>();
    HashSet<Coordinate> lightedSquares = new HashSet<>();

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makePNGList();

        pieces.add(new Piece("King White", new Coordinate(5, 5), ColorWB.WHITE));
        pieces.add(new Piece("Queen White", new Coordinate(2, 1), ColorWB.WHITE));
        pieces.add(new Piece("Rook White", new Coordinate(3, 1), ColorWB.WHITE));
        pieces.add(new Piece("Knight White", new Coordinate(4, 1), ColorWB.WHITE));
        pieces.add(new Piece("Bishop White", new Coordinate(5, 1), ColorWB.WHITE));
        pieces.add(new Piece("Pawn White", new Coordinate(6, 1), ColorWB.WHITE));

    }


    public void makePNGList() {
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
        listOfPiecesAndPNG.put("Test", BitmapFactory.decodeResource(getResources(), R.drawable.test_piece));

    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        /*defX = 0;
        defY = 0;
        squareSide = 120;*/
        //   canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом

        drawChessBoard(canvas);                             //отрисовка доски
        drawPieces(canvas);                                 //отрисовка фигур
        lightTheAvailableMoves(canvas, lightedSquares);     //отрисовка доступных ходов
    }

    private void drawPieces(Canvas canvas) {            //рисует все фигуры из листа pieces
        for (Piece i : pieces) {
            drawPieceAt(canvas, listOfPiecesAndPNG.get(i.getName()), squareToDown(squares.get(i.getCoordinates())));
        }
    }

    private RectF squareToDown(RectF square) {              //уменьшает входящий квадрат на размер indent

        return new RectF(square.left + indent, square.top + indent, square.right - indent, square.bottom - indent);
    }

    private void drawPieceAt(Canvas canvas, Bitmap bitmap, RectF rect) {        //рисует фигуру по указанному квадрату
        canvas.drawBitmap(bitmap, null, rect, paint);
    }

    public void drawChessBoard(Canvas canvas) {
        boolean colorWB = true;
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                if (colorWB) {
                    paint.setColor(Color.GRAY);
                } else {
                    paint.setColor(Color.parseColor("#C31107"));
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

    private void lightTheAvailableMoves(Canvas canvas, HashSet<Coordinate> coordinates) {
        paint.setColor(Color.WHITE);
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius0fFreeMovePoint, paint);
        }

    }

    private boolean canMoveThere(Coordinate coordinate) {
        for (Coordinate coor : lightedSquares) {
            if (coordinate.equals(coor)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkBordersOfView(float x, float y) {           //проверка не выходит ли указатель за рамки view при перемещении фигуры
        return x < MainActivity.board.getLeft() || x > MainActivity.board.getRight() || y < MainActivity.board.getTop() || y > MainActivity.board.getBottom();
    }

    private void makeMove(Piece oldPiece, Piece newPiece) {

        MainActivity.coor.setText(String.format("%s-%s", oldPiece.getCoordinates(), newPiece.getCoordinates()));

        pieces.remove(oldPiece);
        pieces.add(newPiece);
        lightedSquares.clear();
        Log.d(TAG, "makeMove");
        invalidate();
        Log.d(TAG, pieces.toString());
    }

    private Coordinate isVerticalFree(Coordinate coordinates) {
        for (Piece piece : pieces) {
            if (piece.getCoordinates().number == coordinates.number) {
                return piece.getCoordinates();
            }
        }
        return coordinates;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {


        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!checkBordersOfView(event.getX(), event.getY())) {
                Coordinate currentCoor = new Coordinate(1 + ((int) (event.getX() - defX) / squareSide), (int) (9 - (event.getY() - defY) / squareSide));

                if (isPieceAtCoordinates(currentCoor)) {
                    defaultPiece = getPieceAtCoordinates(currentCoor);
                    fillListOfLightedSquares(defaultPiece.getName(), defaultPiece.getCoordinates());
                    Log.d(TAG, "Тут есть фигура " + defaultPiece.getName() + " " + defaultPiece.getCoordinates());
                }

                if (!isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece newPiece = new Piece(defaultPiece.getName(), currentCoor, defaultPiece.getColorWB());
                    Log.d(TAG, "Сюда можно пойти - " + currentCoor);
                    makeMove(defaultPiece, newPiece);

                }

                if (!isPieceAtCoordinates(currentCoor)) {
                    Log.d(TAG, "square is empty");
                    lightedSquares.clear();
                    invalidate();
                }
            }

        }

        return true;
    }


    private void fillListOfLightedSquares(String name, Coordinate coordinates) {
        if (name.equals("King White") || name.equals("King Black")) {
            if (lightedSquares.isEmpty()) {
                for (int j = 1; j >= -1; j--) {
                    for (int i = 1; i >= -1; i--) {
                        lightedSquares.add(new Coordinate(coordinates.letter - j, coordinates.number - i));
                    }
                }
                Log.d(TAG, "Kinggg");
            } else {
                lightedSquares.clear();
            }

        }

        if (name.equals("Rook White") || name.equals("Rook Black")) {
            if (lightedSquares.isEmpty()) {

                for (int i = coordinates.number + 1; i <= 8; i++) {          //проход по вертикали вверх
                    if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, i))) {
                        lightedSquares.add(new Coordinate(coordinates.letter, i));
                    } else {
                        break;
                    }
                }
                for (int i = coordinates.number - 1; i >= 1; i--) {          //проход по вертикали вниз
                    if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, i))) {
                        lightedSquares.add(new Coordinate(coordinates.letter, i));
                    } else {
                        break;
                    }
                }

                for (int i = coordinates.letter + 1; i <= 8; i++) {          //проход по горизонтали вверх
                    if (!isPieceAtCoordinates(new Coordinate(i, coordinates.number))) {
                        lightedSquares.add(new Coordinate(i, coordinates.number));
                    } else {
                        break;
                    }
                }

                for (int i = coordinates.letter - 1; i >= 1; i--) {          //проход по горизонтали вниз
                    if (!isPieceAtCoordinates(new Coordinate(i, coordinates.number))) {
                        lightedSquares.add(new Coordinate(i, coordinates.number));
                    } else {
                        break;
                    }
                }

            } else {
                lightedSquares.clear();
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
