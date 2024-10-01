package com.example.chessandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.example.chessandroid.enums.ColorOfPiece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;


//TODO: Рокировка (castling)
//TODO: Взятие на проходе (en passant)
//TODO: Превращение пешки (promotion)
//TODO: буквы и цифры
//TODO: сохранение взятых фигур
//TODO: пат и ничья (stalemate and draw)

public class Board extends View {
    Piece bufferPiece = new Piece();
    Piece testPiece = new Piece("EmptyPlace", new Coordinate(8, 8), ColorOfPiece.TEST, false);
    Paint paint = new Paint();

    //ПЕРЕМЕННЫЕ
    final private String TAG = "MainActivity";
    float scaleFactor = 1f;                   //коэффициент размера доски
    float chessBoardSize;                       //размер всей доски
    int squareSide;                         //размер квадрата
    float defY;                          //координата У для начала отсчета доски
    float defX;                            //координата Х для начала отсчета доски
    float radius0fFreeMovePoint = 15;    //радиус точки, указывающей на доступные ходы
    float indent = 5;                   //размер отступа внутри клетки до фигуры
    static int INDEX_OF_COLOR = 3;          //вариант дизайна фигур
    static boolean isWhiteMoving;       //проверка на очередность ходов
    static boolean isInvertedBoard = false; //проверка, является ли доска инвертированной
    static boolean isCheck = false;         //флаг шаха
    static boolean isCheckMate = false;         //флаг мата
    static boolean isCheckTheWhite = false;         //шах белому королю
    static boolean isCheckTheBlack = false;         //шах черному королю
    ColorOfPiece checkColor;                    //цвет фигуры, которой шах


    //МАССИВЫ И ЛИСТЫ
    static ArrayList<Integer> timeDrawMoves = new ArrayList<>();
    static ArrayList<Piece> pieces = new ArrayList<>();
    static LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    static HashSet<Coordinate> allowedMovesList = new HashSet<>();
    static HashSet<Coordinate> BUSY_COORDINATES = new HashSet<>();
    static LinkedList<History> history = new LinkedList<>();
    HashMap<String, Bitmap> listOfPiecesAndPNG = Bitmaps.listOfPiecesAndPNG;
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
                    history.add(new History(bufferPiece, "eat", sacrificePiece));
                    check();
                    afterMove(history.getLast());

                }
                if (!isPieceAtCoordinates(currentCoor) && canMoveThere(currentCoor)) {
                    Piece gotoPiece = new Piece(bufferPiece.getName(), currentCoor, bufferPiece.getColorOfPiece(), false);
                    makeMove(bufferPiece, gotoPiece);
                    history.add(new History(bufferPiece, "move", gotoPiece));
                    check();
                    afterMove(history.getLast());
                }
                if (!isPieceAtCoordinates(currentCoor)) {
                    allowedMovesList.clear();
                }
                invalidate();
            }
        }
        return true;
    }

    private static void fillBusyCoordinatesList() {
        BUSY_COORDINATES.clear();
        for (Piece piece : pieces) {
            BUSY_COORDINATES.add(piece.getCoordinates());
        }
    }

    private boolean isPieceAtCoordinates(Coordinate coordinate) {     //проверяет стоит ли на указанном по координатам квадрате фигура
        return BUSY_COORDINATES.contains(coordinate);
        /*for (Piece i : pieces) {
            if (i.getCoordinates().equals(coordinate)) {
                return true;
            }
        }

        return false;*/
    }

    public void makeBackMoveByRecord(History history) {

        switch (history.getAction()) {
            case ("move"):
                Piece from = history.getFrom();
                Piece to = history.getTo();
                makeBackMove(from, to);
                break;

            case ("eat"):
                Piece attackPiece = history.getFrom();
                Piece sacrificePiece = history.getTo();
                makeBackEat(attackPiece, sacrificePiece);
                break;
        }
        check();
        isWhiteMoving = !isWhiteMoving;
        allowedMovesList.clear();
        MainActivity.checkmate.setText("");
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

    public void makeBackEat(Piece oldAttackPiece, Piece sacrificePiece) {
        Piece newAttackPiece = new Piece(oldAttackPiece.getName(), sacrificePiece.getCoordinates(), oldAttackPiece.getColorOfPiece(), false);

        pieces.remove(newAttackPiece);
        pieces.add(oldAttackPiece);
        pieces.add(sacrificePiece);
        fillBusyCoordinatesList();
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

    public void afterMove(History lastAction) {
        isWhiteMoving = !isWhiteMoving;
        allowedMovesList.clear();
        setCheckText(isCheck);                              //пишет когда кому шах
        if (isCheck) {
            checkMate();
        }
        setCheckMateText(isCheckMate);
    }

    public void writeWhoIsMove() {//ПИШЕТ КОГДА ЧЕЙ ХОД
        if (isWhiteMoving) {
            MainActivity.move.setText(String.format("%s", "Белые ходят"));
        } else {
            MainActivity.move.setText(String.format("%s", "Черные ходят"));
        }
    }

    private void setCheckMateText(boolean isCheckMate) {
        if (isCheckMate) {
            if (!isCheckTheWhite) {
                MainActivity.checkmate.setText(String.format("%s", "Мат черным!"));
            }
            if (isCheckTheWhite) {
                MainActivity.checkmate.setText(String.format("%s", "Мат белым!"));
            }
            MainActivity.move.setText(String.format("%s", "Пиздец"));
        }
    }

    public void setCheckText(boolean isCheck) {
        if (isCheck) {

            if (!isCheckTheWhite) {
                MainActivity.checkmate.setText(String.format("%s", "Шах черным!"));
            }
            if (isCheckTheWhite) {
                MainActivity.checkmate.setText(String.format("%s", "Шах белым!"));
            }
        } else {
            MainActivity.checkmate.setText("");
        }
    }

    public static void makeDefaultPlacement() { //УСТАНАВЛИВАЕТ НАЧАЛЬНУЮ РАССТАНОВКУ ФИГУР
        pieces.clear();
        allowedMovesList.clear();
        history.clear();
        isWhiteMoving = true;
        isCheck = false;


       /* pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorOfPiece.BLACK, true));
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
        pieces.add(new Piece("King Black", new Coordinate(5, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Rook Black", new Coordinate(1, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Rook Black", new Coordinate(8, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Knight Black", new Coordinate(2, 8), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Pawn Black", new Coordinate(1, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Pawn Black", new Coordinate(2, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Pawn Black", new Coordinate(3, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Pawn White", new Coordinate(3, 2), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Pawn Black", new Coordinate(7, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Pawn Black", new Coordinate(8, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Bishop Black", new Coordinate(6, 5), ColorOfPiece.BLACK, false));
        pieces.add(new Piece("King White", new Coordinate(5, 1), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Pawn White", new Coordinate(1, 2), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Pawn White", new Coordinate(7, 2), ColorOfPiece.WHITE, true));
        pieces.add(new Piece("Pawn White", new Coordinate(4, 4), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Queen Black", new Coordinate(3, 3), ColorOfPiece.BLACK, false));
        pieces.add(new Piece("Pawn Black", new Coordinate(5, 4), ColorOfPiece.BLACK, false));
        pieces.add(new Piece("Pawn Black", new Coordinate(5, 5), ColorOfPiece.BLACK, false));
        pieces.add(new Piece("Bishop White", new Coordinate(3, 4), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Bishop Black", new Coordinate(2, 4), ColorOfPiece.BLACK, false));
        pieces.add(new Piece("Bishop White", new Coordinate(4, 2), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Queen White", new Coordinate(5, 2), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Knight Black", new Coordinate(8, 2), ColorOfPiece.BLACK, false));
        pieces.add(new Piece("Pawn Black", new Coordinate(6, 7), ColorOfPiece.BLACK, true));
        pieces.add(new Piece("Pawn White", new Coordinate(6, 3), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Knight White", new Coordinate(6, 2), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Rook White", new Coordinate(6, 1), ColorOfPiece.WHITE, false));
        pieces.add(new Piece("Rook White", new Coordinate(3, 1), ColorOfPiece.WHITE, false));

        fillBusyCoordinatesList();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом
        drawChessBoard(canvas);                             //отрисовка доски
        drawPieces(canvas);                                 //отрисовка фигур
        lightAllowedMoves(canvas, allowedMovesList);     //отрисовка доступных ходов
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
            drawPieceAt(canvas, listOfPiecesAndPNG.get(i.getName()), reduceThePiece(squares.get(i.getCoordinates())));
        }
    }

    private RectF reduceThePiece(RectF square) {              //уменьшает входящий квадрат на размер indent
        return new RectF(square.left + indent, square.top + indent, square.right - indent, square.bottom - indent);
    }

    private void drawPieceAt(Canvas canvas, Bitmap bitmap, RectF rect) {        //рисует фигуру по указанному квадрату
        canvas.drawBitmap(bitmap, null, rect, paint);
    }

    public void drawChessBoard(Canvas canvas) {
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
        defY = 0;
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
        paint.setColor(Color.parseColor(colors.get("Цвет выделения свободного хода")));        //цвет выделения свободных ходов
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius0fFreeMovePoint, paint);
        }
    }

    private boolean canMoveThere(Coordinate coordinate) { //проверяет, может ли фигура пойти на указанные координаты
        return allowedMovesList.contains(coordinate);
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

        invalidate();
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

    /*private String adapterPieceNames(String name) {
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
    }*/
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
