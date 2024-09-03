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

    Piece fromPiece = new Piece("Test", new Coordinate(8, 8), ColorWB.WHITE, true); //тестовая фигура, всплывает если что то не так
    Paint paint = new Paint();


    final private String TAG = "MainActivity";
    int defX = 0;                   //координата Х для начала отсчета доски
    int defY = 0;                   //координата У для начала отсчета доски
    int squareSide = 120;           //размер квадрата
    float radius0fFreeMovePoint = 15;    //радиус точки, указывающей на доступные ходы
    float indent = 5;                   //размер отступа внутри клетки до фигуры
    static boolean isWhiteMoving = true;


    static ArrayList<Piece> pieces = new ArrayList<>();
    static LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    HashMap<String, Bitmap> listOfPiecesAndPNG = new HashMap<>();
    static HashSet<Coordinate> lightedSquares = new HashSet<>();

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makePNGList();
        makeDefaultPlacement();

    }

    public static void makeDefaultPlacement() {
        pieces.clear();
        lightedSquares.clear();
        isWhiteMoving = true;

        pieces.add(new Piece("Rook Black", new Coordinate(1, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("Knight Black", new Coordinate(2, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("Bishop Black", new Coordinate(3, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("Queen Black", new Coordinate(4, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("Bishop Black", new Coordinate(6, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("Knight Black", new Coordinate(7, 8), ColorWB.BLACK, true));
        pieces.add(new Piece("Rook Black", new Coordinate(8, 8), ColorWB.BLACK, true));


        pieces.add(new Piece("Rook White", new Coordinate(1, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("Knight White", new Coordinate(2, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("Bishop White", new Coordinate(3, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("Queen White", new Coordinate(4, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("King White", new Coordinate(5, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("Bishop White", new Coordinate(6, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("Knight White", new Coordinate(7, 1), ColorWB.WHITE, true));
        pieces.add(new Piece("Rook White", new Coordinate(8, 1), ColorWB.WHITE, true));

        for (int i = 1; i <= 8; i++) {
            pieces.add(new Piece("Pawn White", new Coordinate(i, 2), ColorWB.WHITE, true));
            pieces.add(new Piece("Pawn Black", new Coordinate(i, 7), ColorWB.BLACK, true));
        }


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

    public void writeWhoIsMove() {
        if (isWhiteMoving) {
            MainActivity.move.setText(String.format("%s", "White is moving"));
        } else {
            MainActivity.move.setText(String.format("%s", "Black is moving"));
        }
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
        writeWhoIsMove();
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
        paint.setColor(Color.GREEN);        //цвет выделения свободных ходов
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

    private void makeMove(Piece fromPiece, Piece gotoPiece) {

        MainActivity.coor.setText(String.format("%s%s-%s", adapterPieceNames(fromPiece.getName()), fromPiece.getCoordinates(), gotoPiece.getCoordinates()));

        pieces.remove(fromPiece);
        pieces.add(gotoPiece);
        gotoPiece.setFirstMove(false);
        lightedSquares.clear();
        isWhiteMoving = !isWhiteMoving;
        Log.d(TAG, "Move is made");
        invalidate();
    }

    private void eatThePiece(Piece fromPiece, Piece killerPiece, Piece victimPiece) {
        MainActivity.coor.setText(String.format("%s%sx%s", adapterPieceNames(killerPiece.getName()), fromPiece.getCoordinates(), victimPiece.getCoordinates()));
        pieces.remove(fromPiece);
        pieces.remove(victimPiece);
        pieces.add(killerPiece);
        lightedSquares.clear();
        isWhiteMoving = !isWhiteMoving;
        Log.d(TAG, "eaten");

    }

    private String adapterPieceNames(String name) {
        switch (name) {
            case ("King Black"):
                return "♚";
            case ("Queen Black"):
                return "♛";
            case ("Rook Black"):
                return "♜";
            case ("Knight Black"):
                return "♞";
            case ("Bishop Black"):
                return "♝";
            case ("Pawn Black"):
                return "♟";

            case ("King White"):
                return "♔";
            case ("Queen White"):
                return "♕";
            case ("Rook White"):
                return "♖";
            case ("Knight White"):
                return "♘";
            case ("Bishop White"):
                return "♗";
            case ("Pawn White"):
                return "♙";

        }
        return " ";
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {


        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!checkBordersOfView(event.getX(), event.getY())) {
                Coordinate currentCoor = new Coordinate(1 + ((int) (event.getX() - defX) / squareSide), (int) (9 - (event.getY() - defY) / squareSide));


                if (isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece killerPiece = new Piece(fromPiece.getName(), currentCoor, fromPiece.getColorWB(), true);
                    Piece victimPiece = getPieceAtCoordinates(currentCoor);
                    Log.d(TAG, "Сюда можно пойти и съесть - " + currentCoor);
                    eatThePiece(fromPiece, killerPiece, victimPiece);
                    invalidate();

                }

                if (isPieceAtCoordinates(currentCoor) && getPieceAtCoordinates(currentCoor).getColorWB() == ColorWB.WHITE && isWhiteMoving) {
                    fromPiece = getPieceAtCoordinates(currentCoor);
                    fillListOfLightedSquares(fromPiece);

                    Log.d(TAG, "Фигура выделена: " + fromPiece.getName() + " " + fromPiece.getCoordinates() + " " + fromPiece.isFirstMove());

                }
                if (isPieceAtCoordinates(currentCoor) && getPieceAtCoordinates(currentCoor).getColorWB() == ColorWB.BLACK && !isWhiteMoving) {
                    fromPiece = getPieceAtCoordinates(currentCoor);
                    fillListOfLightedSquares(fromPiece);

                    Log.d(TAG, "Фигура выделена: " + fromPiece.getName() + " " + fromPiece.getCoordinates() + " " + fromPiece.isFirstMove());

                }

                if (!isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece gotoPiece = new Piece(fromPiece.getName(), currentCoor, fromPiece.getColorWB(), true);
                    Log.d(TAG, "Сюда можно пойти - " + currentCoor);
                    makeMove(fromPiece, gotoPiece);

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


    private void fillListOfLightedSquares(Piece piece) {
        Coordinate coordinates = piece.getCoordinates();
        if (piece.getName().equals("King White") || piece.getName().equals("King Black")) {
            if (lightedSquares.isEmpty()) {
                for (int j = 1; j >= -1; j--) {
                    for (int i = 1; i >= -1; i--) {
                        lightedSquares.add(new Coordinate(coordinates.letter - j, coordinates.number - i));
                    }
                }
            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }

        if (piece.getName().equals("Rook White") || piece.getName().equals("Rook Black")) {
            if (lightedSquares.isEmpty()) {
                verticalAndHorizontalPassage(coordinates);
            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }

        if (piece.getName().equals("Bishop White") || piece.getName().equals("Bishop Black")) {
            if (lightedSquares.isEmpty()) {
                diagonalPassage(coordinates);
            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }

        if (piece.getName().equals("Queen White") || piece.getName().equals("Queen Black")) {
            if (lightedSquares.isEmpty()) {
                diagonalPassage(coordinates);
                verticalAndHorizontalPassage(coordinates);
            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }

        if (piece.getName().equals("Pawn White")) {
            if (lightedSquares.isEmpty()) {
                if (piece.isFirstMove()) {
                    for (int i = 1; i <= 2; i++) {
                        if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number + i))) {
                            lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number + i));
                        }
                    }
                }
                if (!piece.isFirstMove()) {
                    if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number + 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number + 1));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter-1, coordinates.number + 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter-1, coordinates.number + 1));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter+1, coordinates.number + 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter+1, coordinates.number + 1));
                    }
                }
            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }

        if (piece.getName().equals("Pawn Black")) {
            if (lightedSquares.isEmpty()) {
                if (piece.isFirstMove()) {
                    for (int i = 1; i <= 2; i++) {
                        if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number - i))) {
                            lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number - i));
                        }
                    }
                }
                if (!piece.isFirstMove()) {
                    if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number - 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number - 1));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter+1, coordinates.number - 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter+1, coordinates.number - 1));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter-1, coordinates.number - 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter-1, coordinates.number - 1));
                    }
                }
            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }

        if (piece.getName().equals("Knight White") || piece.getName().equals("Knight Black")) {
            if (lightedSquares.isEmpty()) {
                lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number + 2));
                lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number - 2));
                lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number + 2));
                lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number - 2));

                lightedSquares.add(new Coordinate(coordinates.letter + 2, coordinates.number - 1));
                lightedSquares.add(new Coordinate(coordinates.letter + 2, coordinates.number + 1));
                lightedSquares.add(new Coordinate(coordinates.letter - 2, coordinates.number + 1));
                lightedSquares.add(new Coordinate(coordinates.letter - 2, coordinates.number - 1));

            } else {
                lightedSquares.clear();
            }
            lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorWB() == piece.getColorWB());
        }


        lightedSquares.removeIf(coor -> coor.letter > 8
                || coor.letter < 1
                || coor.number > 8
                || coor.number < 1);
        invalidate();


    }

    private void verticalAndHorizontalPassage(Coordinate coordinates) {     //ищет доступные ходы для фигуры по вертикали и горизонтали

        for (int i = coordinates.number + 1; i <= 8; i++) {          //проход по вертикали вверх
            if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, i))) {
                lightedSquares.add(new Coordinate(coordinates.letter, i));
            } else {
                lightedSquares.add(new Coordinate(coordinates.letter, i));
                break;
            }
        }
        for (int i = coordinates.number - 1; i >= 1; i--) {          //проход по вертикали вниз
            if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, i))) {
                lightedSquares.add(new Coordinate(coordinates.letter, i));
            } else {
                lightedSquares.add(new Coordinate(coordinates.letter, i));
                break;
            }
        }

        for (int i = coordinates.letter + 1; i <= 8; i++) {          //проход по горизонтали вверх
            if (!isPieceAtCoordinates(new Coordinate(i, coordinates.number))) {
                lightedSquares.add(new Coordinate(i, coordinates.number));
            } else {
                lightedSquares.add(new Coordinate(i, coordinates.number));
                break;
            }
        }

        for (int i = coordinates.letter - 1; i >= 1; i--) {          //проход по горизонтали вниз
            if (!isPieceAtCoordinates(new Coordinate(i, coordinates.number))) {
                lightedSquares.add(new Coordinate(i, coordinates.number));
            } else {
                lightedSquares.add(new Coordinate(i, coordinates.number));
                break;
            }
        }
    }

    private void diagonalPassage(Coordinate coordinates) { //ищет доступные ходы для фигуры по диагоналям
        for (int i = coordinates.letter + 1, j = coordinates.number + 1; i <= 8 && j <= 8; i++, j++) {  //проход по правой диагонали вверх
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                lightedSquares.add(new Coordinate(i, j));
            } else {
                lightedSquares.add(new Coordinate(i, j));
                break;
            }
        }
        for (int i = coordinates.letter - 1, j = coordinates.number - 1; i >= 1 && j >= 1; i--, j--) {  //проход по правой диагонали вниз
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                lightedSquares.add(new Coordinate(i, j));
            } else {
                lightedSquares.add(new Coordinate(i, j));
                break;
            }
        }
        for (int i = coordinates.letter - 1, j = coordinates.number + 1; i >= 1 && j <= 8; i--, j++) {  //проход по левой диагонали вверх
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                lightedSquares.add(new Coordinate(i, j));
            } else {
                lightedSquares.add(new Coordinate(i, j));
                break;
            }
        }
        for (int i = coordinates.letter + 1, j = coordinates.number - 1; i <= 8 && j >= 1; i++, j--) {  //проход по левой диагонали вниз
            if (!isPieceAtCoordinates(new Coordinate(i, j))) {
                lightedSquares.add(new Coordinate(i, j));
            } else {
                lightedSquares.add(new Coordinate(i, j));
                break;
            }
        }
    }
}
