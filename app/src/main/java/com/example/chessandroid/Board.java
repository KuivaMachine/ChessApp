package com.example.chessandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import com.example.chessandroid.enums.ColorOfPiece;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

//СДЕЛАТЬ НОРМАЛЬНУЮ ЗАПИСЬ ХОДОВ = КТО, ОТКУДА, ИМЯ И ЦВЕТ, координаты
//TODO: История ходов
//TODO: Шах и мат
//TODO: Рокировка (castling)
//TODO: Взятие на проходе (en passant)
//TODO: Превращение пешки (promotion)
//TODO: буквы и цифры
//TODO: сохранение взятых фигур
//TODO: пат и ничья (stalemate and draw)

public class Board extends View {

    Piece fromPiece = new Piece();
    Paint paint = new Paint();


    //ПЕРЕМЕННЫЕ
    final private String TAG = "MainActivity";
    float scaleFactor = 1f;                   //размер отступа внутри клетки до фигуры
    float chessBoardSize;                       //размер всей доски
    int squareSide;                         //размер квадрата
    float defY;                          //координата У для начала отсчета доски
    float defX;                            //координата Х для начала отсчета доски
    float radius0fFreeMovePoint = 15;    //радиус точки, указывающей на доступные ходы
    float indent = 5;                   //размер отступа внутри клетки до фигуры
    static boolean isWhiteMoving;       //проверка на очередность ходов
    static boolean isInvertedBoard = false; //проаверка, является ли доска инвертированной
    static boolean isCheck = false;         //флаг шаха
    ColorOfPiece checkColor;                 //цвет короля, которому сделан шах


    //МАССИВЫ И ЛИСТЫ
    static ArrayList<Piece> pieces = new ArrayList<>();
    static LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    static HashSet<Coordinate> lightedSquares = new HashSet<>();
    static LinkedList<String> moveRecord = new LinkedList<>();
    HashMap<String, Bitmap> listOfPiecesAndPNG = Bitmaps.listOfPiecesAndPNG;
    HashMap<String, String> colors = Bitmaps.colors;

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        makeDefaultPlacement();

        new Bitmaps(this.getContext());

    }

    public void makeBackMove(String line) {
     //   String line = "Rook White\r12\reat\rRook Black\r24";

        String[] parts = line.split("\r");
        Log.d(TAG, String.valueOf(moveRecord));

        switch (parts[2]) {
            case ("move"):
                Piece from = new Piece(parts[0], new Coordinate(Integer.parseInt(String.valueOf(parts[1].charAt(0))), Integer.parseInt(String.valueOf(parts[1].charAt(1)))), ColorOfPiece.WHITE, false);
                Piece to = getPieceAtCoordinates(new Coordinate(Integer.parseInt(String.valueOf(parts[3].charAt(0))), Integer.parseInt(String.valueOf(parts[3].charAt(1)))));
                if (parts[0].endsWith("Black")) {
                    from.setColorOfPiece(ColorOfPiece.BLACK);
                    to.setColorOfPiece(ColorOfPiece.BLACK);
                }
                makeMove(to, from);
                //не может ходить назад под шах
                break;
            case ("eat"):
                Piece attackPiece = getPieceAtCoordinates(new Coordinate(Integer.parseInt(String.valueOf(parts[4].charAt(0))), Integer.parseInt(String.valueOf(parts[4].charAt(1)))));
                Piece fromPiece = new Piece(attackPiece.getName(), new Coordinate(Integer.parseInt(String.valueOf(parts[1].charAt(0))), Integer.parseInt(String.valueOf(parts[1].charAt(1)))), attackPiece.getColorOfPiece(), false);
                Piece sacrificePiece = new Piece(parts[3], new Coordinate(Integer.parseInt(String.valueOf(parts[4].charAt(0))), Integer.parseInt(String.valueOf(parts[4].charAt(1)))), ColorOfPiece.WHITE, false);
                if (parts[3].endsWith("Black")) {
                    sacrificePiece.setColorOfPiece(ColorOfPiece.BLACK);
                }
                //надо добавитть логику

                break;
        }


    }

    public static void makeDefaultPlacement() { //УСТАНАВЛИВАЕТ НАЧАЛЬНУЮ РАССТАНОВКУ ФИГУР
        pieces.clear();
        lightedSquares.clear();
        isWhiteMoving = false;
        isCheck = false;

        pieces.add(new Piece("Rook White", new Coordinate(1, 5), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Rook Black", new Coordinate(2, 5), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Knight White", new Coordinate(2, 4), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("King Black", new Coordinate(6, 5), ColorOfPiece.BLACK, true));
        //pieces.add(new Piece("Rook Black", new Coordinate(2, 6), ColorOfPiece.BLACK, true));
        // pieces.add(new Piece("King White", new Coordinate(6, 5), ColorOfPiece.WHITE, true));

        /*pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorOfPiece.BLACK, true));
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

    public void writeWhoIsMove() {//ПИШЕТ КОГДА ЧЕЙ ХОД
        if (isWhiteMoving) {

            MainActivity.move.setText(String.format("%s", "Белые ходят"));
        } else {
            MainActivity.move.setText(String.format("%s", "Черные ходят"));
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом
        drawChessBoard(canvas);                             //отрисовка доски
        drawPieces(canvas);                                 //отрисовка фигур
        lightTheAvailableMoves(canvas, lightedSquares);     //отрисовка доступных ходов
        writeWhoIsMove();                                   //пишет чей ход


    }

    public void initialisationOfXY() { //т.к. методы getWidth() и getHeight() (View) не хотят возвращать значения при начальной инициализации, придется сделать ее в отдельном методе
        chessBoardSize = Math.min(getWidth(), getHeight()) * scaleFactor;
        squareSide = (int) (chessBoardSize / 8);
        defX = (getWidth() - chessBoardSize) / 2f;
        defY = (getHeight() - chessBoardSize) / 16f;

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
        initialisationOfXY();

        boolean colorWB = true;
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                if (colorWB) {
                    paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));
                } else {
                    paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));
                }
                colorWB = !colorWB;
                canvas.drawRect(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide), paint);
                if (isInvertedBoard) {
                    squares.put(new Coordinate(8 - i, j + 1), new RectF(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide)));
                } else {
                    squares.put(new Coordinate(i + 1, 8 - j), new RectF(defX + (squareSide * i), defY, defX + (squareSide * (i + 1)), (defY + squareSide)));
                }
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
        paint.setColor(Color.parseColor(colors.get("Цвет выделения свободного хода")));        //цвет выделения свободных ходов
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius0fFreeMovePoint, paint);
        }
    }

    private boolean canMoveThere(Coordinate coordinate) { //проверяет, может ли фигура пойти на указанные координаты
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

    private void makeMove(Piece fromPiece, Piece gotoPiece) {

        pieces.remove(fromPiece);
        pieces.add(gotoPiece);
        check();

        if (check() == 0) {
            MainActivity.coor.setText(String.format("%s%s-%s", adapterPieceNames(fromPiece.getName()), fromPiece.getCoordinates(), gotoPiece.getCoordinates()));
            gotoPiece.setFirstMove(false);
            isWhiteMoving = !isWhiteMoving;
        }
        if (check() > 0) {
            if (gotoPiece.getColorOfPiece() != checkColor) {
                MainActivity.coor.setText(String.format("%s%s-%s", adapterPieceNames(fromPiece.getName()), fromPiece.getCoordinates(), gotoPiece.getCoordinates()));
                gotoPiece.setFirstMove(false);
                isWhiteMoving = !isWhiteMoving;
            } else {
                MainActivity.coor.setText("CHECK!");
                pieces.remove(gotoPiece);
                pieces.add(fromPiece);
            }

        }

        lightedSquares.clear();


    }

    private void eatThePiece(Piece fromPiece, Piece attackPiece, Piece sacrificePiece) {
        pieces.remove(fromPiece);
        pieces.remove(sacrificePiece);
        pieces.add(attackPiece);
        check();

        if (check() == 0) {
            MainActivity.coor.setText(String.format("%s%sx%s", adapterPieceNames(attackPiece.getName()), fromPiece.getCoordinates(), sacrificePiece.getCoordinates()));
            attackPiece.setFirstMove(false);
            isWhiteMoving = !isWhiteMoving;
        }
        if (check() > 0) {
            if (attackPiece.getColorOfPiece() != checkColor) {
                MainActivity.coor.setText(String.format("%s%sx%s", adapterPieceNames(attackPiece.getName()), fromPiece.getCoordinates(), sacrificePiece.getCoordinates()));
                attackPiece.setFirstMove(false);
                isWhiteMoving = !isWhiteMoving;
            } else {
                MainActivity.coor.setText("CHECK!");
                pieces.remove(attackPiece);
                pieces.add(fromPiece);
                pieces.add(sacrificePiece);
            }

        }

        lightedSquares.clear();

    }

    private String adapterPieceNames(String name) {
        switch (name) {
            case ("King Black"):
            case ("King White"):
                return "Кр";
            case ("Queen Black"):
            case ("Queen White"):
                return "Ф";
            case ("Rook Black"):
            case ("Rook White"):
                return "Л";
            case ("Knight Black"):
            case ("Knight White"):
                return "К";
            case ("Bishop Black"):
            case ("Bishop White"):
                return "С";
            case ("Pawn Black"):
            case ("Pawn White"):
                return "";
        }
        return "SAS";
    }

    private int check() {
        int countOfChecks = 0;

        for (Piece piece : pieces) {
            canThePieceMove(piece);
            for (Coordinate coor : lightedSquares) {
                if (isPieceAtCoordinates(coor)) {
                    if (getPieceAtCoordinates(coor).getName().equals("King White")) {
                        isCheck = true;
                        checkColor = ColorOfPiece.WHITE;
                        countOfChecks++;
                    }
                    if (getPieceAtCoordinates(coor).getName().equals("King Black")) {
                        isCheck = true;
                        checkColor = ColorOfPiece.BLACK;
                        countOfChecks++;
                    }
                }
            }
        }
        if (countOfChecks == 0) {
            isCheck = false;
        }
        if (isCheck) {
            if (checkColor == ColorOfPiece.BLACK) {
                MainActivity.checkmate.setText(String.format("%s", "Шах черным!"));
            }
            if (checkColor == ColorOfPiece.WHITE) {
                MainActivity.checkmate.setText(String.format("%s", "Шах белым!"));
            }

        } else {
            MainActivity.checkmate.setText("");
        }
        return countOfChecks;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!checkBordersOfView(event.getX(), event.getY())) {
                Coordinate currentCoor = invertedCoordinates(event.getX(), event.getY());
                if (isPieceAtCoordinates(currentCoor) && getPieceAtCoordinates(currentCoor).getColorOfPiece() == ColorOfPiece.WHITE && isWhiteMoving) {
                    fromPiece = getPieceAtCoordinates(currentCoor);
                    canThePieceMove(fromPiece);
                    // Log.d(TAG, "Фигура выделена: " + fromPiece.getName() + " " + fromPiece.getCoordinates() + " " + fromPiece.isFirstMove());
                }
                if (isPieceAtCoordinates(currentCoor) && getPieceAtCoordinates(currentCoor).getColorOfPiece() == ColorOfPiece.BLACK && !isWhiteMoving) {
                    fromPiece = getPieceAtCoordinates(currentCoor);
                    canThePieceMove(fromPiece);
                    //   Log.d(TAG, "Фигура выделена: " + fromPiece.getName() + " " + fromPiece.getCoordinates() + " " + fromPiece.isFirstMove());
                }
                if (isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece attackPiece = new Piece(fromPiece.getName(), currentCoor, fromPiece.getColorOfPiece(), true);
                    Piece sacrifice = getPieceAtCoordinates(currentCoor);
                    // Log.d(TAG, "Сюда можно пойти и съесть - " + currentCoor);
                    eatThePiece(fromPiece, attackPiece, sacrifice);
                    moveRecord.add(attackPiece.getName() + "\r" + attackPiece.getCoordinates() + "\r" + "eat"+ "\r" + sacrifice.getName() + "\r" + sacrifice.getCoordinates());
                    invalidate();
                }
                if (!isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece gotoPiece = new Piece(fromPiece.getName(), currentCoor, fromPiece.getColorOfPiece(), true);
                    //Log.d(TAG, "Сюда можно пойти - " + currentCoor);
                    makeMove(fromPiece, gotoPiece);
                    moveRecord.add(fromPiece.getName() + "\r" + fromPiece.getCoordinates() + "\r" + "move" + "\r" + gotoPiece.getCoordinates());
                    invalidate();
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

    private void canThePieceMove(Piece piece) { //заносит в лист те клетки, куда может пойти указанная фигура, в зав. от ее имени
        Coordinate coordinates = piece.getCoordinates();
        if (!lightedSquares.isEmpty()) {
            lightedSquares.clear();
        }
        if (piece.getName().equals("King White") || piece.getName().equals("King Black")) {

            for (int j = 1; j >= -1; j--) {
                for (int i = 1; i >= -1; i--) {
                    lightedSquares.add(new Coordinate(coordinates.letter - j, coordinates.number - i));
                }
            }

        }

        if (piece.getName().equals("Rook White") || piece.getName().equals("Rook Black")) {

            verticalAndHorizontalPassage(coordinates);

        }

        if (piece.getName().equals("Bishop White") || piece.getName().equals("Bishop Black")) {

            diagonalPassage(coordinates);

        }

        if (piece.getName().equals("Queen White") || piece.getName().equals("Queen Black")) {

            diagonalPassage(coordinates);
            verticalAndHorizontalPassage(coordinates);

        }

        if (piece.getName().equals("Pawn White")) {
            if (piece.isFirstMove()) {
                for (int i = 1; i <= 2; i++) {
                    if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number + i))) {
                        lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number + i));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter - 1, coordinates.number + 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number + 1));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter + 1, coordinates.number + 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number + 1));
                    }
                }
            }
            if (!piece.isFirstMove()) {
                if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number + 1))) {
                    lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number + 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.letter - 1, coordinates.number + 1))) {
                    lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number + 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.letter + 1, coordinates.number + 1))) {
                    lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number + 1));
                }
            }

        }

        if (piece.getName().equals("Pawn Black")) {

            if (piece.isFirstMove()) {
                for (int i = 1; i <= 2; i++) {
                    if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number - i))) {
                        lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number - i));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter + 1, coordinates.number - 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number - 1));
                    }
                    if (isPieceAtCoordinates(new Coordinate(coordinates.letter - 1, coordinates.number - 1))) {
                        lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number - 1));
                    }
                }
            }
            if (!piece.isFirstMove()) {
                if (!isPieceAtCoordinates(new Coordinate(coordinates.letter, coordinates.number - 1))) {
                    lightedSquares.add(new Coordinate(coordinates.letter, coordinates.number - 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.letter + 1, coordinates.number - 1))) {
                    lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number - 1));
                }
                if (isPieceAtCoordinates(new Coordinate(coordinates.letter - 1, coordinates.number - 1))) {
                    lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number - 1));
                }
            }

        }

        if (piece.getName().equals("Knight White") || piece.getName().equals("Knight Black")) {

            lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number + 2));
            lightedSquares.add(new Coordinate(coordinates.letter + 1, coordinates.number - 2));
            lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number + 2));
            lightedSquares.add(new Coordinate(coordinates.letter - 1, coordinates.number - 2));

            lightedSquares.add(new Coordinate(coordinates.letter + 2, coordinates.number - 1));
            lightedSquares.add(new Coordinate(coordinates.letter + 2, coordinates.number + 1));
            lightedSquares.add(new Coordinate(coordinates.letter - 2, coordinates.number + 1));
            lightedSquares.add(new Coordinate(coordinates.letter - 2, coordinates.number - 1));

        }
        lightedSquares.removeIf(i -> isPieceAtCoordinates(i) && getPieceAtCoordinates(i).getColorOfPiece().equals(piece.getColorOfPiece()));
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
    /*private String adapterPieceNames(String name) {
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
    }*/
}
