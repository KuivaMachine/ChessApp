package com.example.chessandroid.View;

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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.chessandroid.ChessGame.Coordinate;
import com.example.chessandroid.ChessGame.Move;
import com.example.chessandroid.ChessGame.Piece;
import com.example.chessandroid.ViewModel.ChessViewModel;
import com.example.chessandroid.databinding.PawnBlackPromotionPopupBinding;
import com.example.chessandroid.databinding.PawnWhitePromotionPopupBinding;
import com.example.chessandroid.enums.ColorOfPiece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;


public class Board extends View {
    Piece bufferPiece = new Piece();

    //ПЕРЕМЕННЫЕ

    final private String TAG = "MainActivity";
    private float scaleFactor = 1f;                   //коэффициент размера доски
    private float chessBoardSize;                       //размер всей доски
    private int squareSide;                         //размер квадрата
    private float defY;                          //координата У для начала отсчета доски
    private float defX;                            //координата Х для начала отсчета доски
    private float radius0fFreeMovePoint = 18;    //радиус точки, указывающей на доступные ходы
    private float indent = 5;                   //размер отступа внутри клетки до фигуры
    static int INDEX_OF_COLOR = 3;          //вариант дизайна фигур
    private boolean isCheckTheWhite;         //шах белому королю
    private boolean isCheckTheBlack;         //шах черному королю


    //МАССИВЫ И ЛИСТЫ

    //  static ArrayList<Piece> takenPiecesWhite = new ArrayList<>();
    //  static ArrayList<Piece> takenPiecesBlack = new ArrayList<>();

    private final LinkedHashMap<Coordinate, RectF> squares = new LinkedHashMap<>();
    private final LinkedHashMap<Coordinate, RectF> takenPiecesRects = new LinkedHashMap<>();
    private final HashSet<Coordinate> possibleMovesList = new HashSet<>();
    private final HashMap<String, Bitmap> listOfPiecesAndPNG = Bitmaps.listOfPiecesAndPNG;
    private final HashMap<String, Bitmap> listOfTakenPiecesAndPNG = Bitmaps.listOfTakenPiecesAndPNG;
    private final HashMap<String, String> colors = Bitmaps.colors;
    private final HashSet<Piece> pieces = new HashSet<>();
    private ChessViewModel viewModel;
    private PopupWindow popupWindow;
    PawnWhitePromotionPopupBinding whiteBinding;
    PawnBlackPromotionPopupBinding blackBinding;

    public Board(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        new Bitmaps(this.getContext());
        //Пришлось добавить проверку "находится ли View в режиме предпросмотра", т.к. в этом режиме вместо контекста активити
        //в ViewModel передается BridgeContext, вызывая ошибку ClassCastException
        if (!isInEditMode()) {
            viewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(ChessViewModel.class);
            viewModel.getPieces().observe((GameActivity) context, list -> {
                this.pieces.clear();
                this.pieces.addAll(list);
                invalidate();
            });

            viewModel.isCheckTheWhiteNow().observe((GameActivity) context, isCheckWhite -> {
                this.isCheckTheWhite = isCheckWhite;
                invalidate();
            });
            viewModel.isCheckTheBlackNow().observe((GameActivity) context, isCheckBlack -> {
                this.isCheckTheBlack = isCheckBlack;
                invalidate();
            });
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        whiteBinding = PawnWhitePromotionPopupBinding.inflate(inflater);
        blackBinding = PawnBlackPromotionPopupBinding.inflate(inflater);
        popupWindow = new PopupWindow(whiteBinding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //При отсоединении View от окна отвязываем подписки
        viewModel.getPieces().removeObservers((GameActivity) getContext());
        viewModel.isCheckTheWhiteNow().removeObservers((GameActivity) getContext());
        viewModel.isCheckTheBlackNow().removeObservers((GameActivity) getContext());

    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (viewModel.isMyTurn()) {
                Coordinate currentCoordinate = getCoordinatesByXY(event.getX(), event.getY());
                popupWindow.dismiss();

                //ЕСЛИ ЗДЕСЬ БЕЛАЯ ФИГУРА И СЕЙЧАС ХОДЯТ БЕЛЫЕ
                if (getPieceAtCoordinates(currentCoordinate, pieces).getColorOfPiece() == ColorOfPiece.WHITE && Objects.equals(viewModel.myColorOfPiece(), "White")) {
                    bufferPiece = getPieceAtCoordinates(currentCoordinate, pieces);
                    viewModel.fillListOfPossibleMoves(bufferPiece, possibleMovesList);
                    viewModel.addCastling(bufferPiece, possibleMovesList, pieces);
                    viewModel.removeDangerMoves(bufferPiece, possibleMovesList);
                }

                //ЕСЛИ ЗДЕСЬ ЧЕРНАЯ ФИГУРА И СЕЙЧАС ХОДЯТ ЧЕРНЫЕ
                if (getPieceAtCoordinates(currentCoordinate, pieces).getColorOfPiece() == ColorOfPiece.BLACK && Objects.equals(viewModel.myColorOfPiece(), "Black")) {
                    bufferPiece = getPieceAtCoordinates(currentCoordinate, pieces);
                    viewModel.fillListOfPossibleMoves(bufferPiece, possibleMovesList);
                    viewModel.addCastling(bufferPiece, possibleMovesList, pieces);
                    viewModel.removeDangerMoves(bufferPiece, possibleMovesList);
                }

                //ЕСЛИ МЫ ИДЕМ ПЕШКОЙ НА ПОСЛЕДНЮЮ ГОРИЗОНТАЛЬ
                if (canMoveThere(currentCoordinate) && bufferPiece.getName().startsWith("Pawn") && (currentCoordinate.getNumber() == 8 || currentCoordinate.getNumber() == 1)) {
                    Piece sacrifacePiece = getPieceAtCoordinates(currentCoordinate, pieces);
                    RectF rectF = squares.get(currentCoordinate);
                    //ПРЕВРАЩЕНИЕ БЕЛОЙ ПЕШКИ В ФИГУРУ ПО ВЫБОРУ ИЗ POPUP WINDOW
                    if (currentCoordinate.getNumber() == 8) {
                        if (rectF != null) {
                            popupWindow.showAtLocation(getRootView(), Gravity.NO_GRAVITY, (int) rectF.left, (int) rectF.top);
                            whiteBinding.rookPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Rook White", currentCoordinate, ColorOfPiece.WHITE, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                            whiteBinding.queenPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Queen White", currentCoordinate, ColorOfPiece.WHITE, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                            whiteBinding.knightPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Knight White", currentCoordinate, ColorOfPiece.WHITE, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                            whiteBinding.bishopPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Bishop White", currentCoordinate, ColorOfPiece.WHITE, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                        }
                    }
                    //ПРЕВРАЩЕНИЕ ЧЕРНОЙ ПЕШКИ В ФИГУРУ ПО ВЫБОРУ ИЗ POPUP WINDOW
                    if (currentCoordinate.getNumber() == 1) {
                        if (rectF != null) {
                            popupWindow.setContentView(blackBinding.getRoot());
                            popupWindow.showAtLocation(getRootView(), Gravity.NO_GRAVITY, (int) rectF.left, (int) rectF.top);
                            blackBinding.rookBlackPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Rook Black", currentCoordinate, ColorOfPiece.BLACK, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                            blackBinding.queenBlackPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Queen Black", currentCoordinate, ColorOfPiece.BLACK, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                            blackBinding.knightBlackPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Knight Black", currentCoordinate, ColorOfPiece.BLACK, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                            blackBinding.bishopBlackPiecePopup.setOnClickListener(v -> {
                                Piece promotionPiece = new Piece("Bishop Black", currentCoordinate, ColorOfPiece.BLACK, false);
                                makePromotionMove(sacrifacePiece, promotionPiece);
                            });
                        }
                    }
                }

                //ЕСЛИ ЗДЕСЬ СТОИТ ФИГУРА И СЮДА МОЖНО ХОДИТЬ
                if (viewModel.isPieceAtCoordinates(currentCoordinate) && canMoveThere(currentCoordinate) && !(bufferPiece.getName().startsWith("Pawn") && (currentCoordinate.getNumber() == 8 || currentCoordinate.getNumber() == 1))) {
                    Piece sacrificePiece = getPieceAtCoordinates(currentCoordinate, pieces);
                    viewModel.eatThePiece(bufferPiece, sacrificePiece);
                    viewModel.afterMove();
                    possibleMovesList.clear();
                    Move move = new Move(bufferPiece, "eat", sacrificePiece);
                    viewModel.sendMoveToFirebase(move);
                }

                //ЕСЛИ ЗДЕСЬ НЕТ ФИГУР И СЮДА МОЖНО ХОДИТЬ
                if (!viewModel.isPieceAtCoordinates(currentCoordinate) && canMoveThere(currentCoordinate) && !(bufferPiece.getName().startsWith("Pawn") && (currentCoordinate.getNumber() == 8 || currentCoordinate.getNumber() == 1))) {
                    Move move;

                    //КОРОТКАЯ РОКИРОВКА
                    if ((bufferPiece.getName().equals("King White") && currentCoordinate.equals(new Coordinate(7, 1)))
                            || (bufferPiece.getName().equals("King Black") && currentCoordinate.equals(new Coordinate(7, 8)))) {
                        viewModel.castling(bufferPiece, currentCoordinate);
                        move = new Move(bufferPiece, "0-0");
                    }

                    //ДЛИННАЯ РОКИРОВКА
                    else if ((bufferPiece.getName().equals("King White") && currentCoordinate.equals(new Coordinate(3, 1)))
                            || (bufferPiece.getName().equals("King Black") && currentCoordinate.equals(new Coordinate(3, 8)))) {
                        viewModel.castling(bufferPiece, currentCoordinate);
                        move = new Move(bufferPiece, "0-0-0");
                    }

                    //ВЗЯТИЕ НА ПРОХОДЕ ДЛЯ БЕЛЫХ
                    else if (((bufferPiece.getName().equals("Pawn White")))
                            && currentCoordinate.equals(new Coordinate(bufferPiece.getCoordinates().getLetter() - 1, bufferPiece.getCoordinates().getNumber() + 1))
                            || currentCoordinate.equals(new Coordinate(bufferPiece.getCoordinates().getLetter() + 1, bufferPiece.getCoordinates().getNumber() + 1))) {
                        viewModel.makeEnPassant(bufferPiece, currentCoordinate);
                        move = new Move(bufferPiece, "en passant", new Piece(bufferPiece.getName(), currentCoordinate, bufferPiece.getColorOfPiece(), false));
                    }

                    //ВЗЯТИЕ НА ПРОХОДЕ ДЛЯ ЧЕРНЫХ
                    else if (((bufferPiece.getName().equals("Pawn Black")))
                            && currentCoordinate.equals(new Coordinate(bufferPiece.getCoordinates().getLetter() - 1, bufferPiece.getCoordinates().getNumber() - 1))
                            || currentCoordinate.equals(new Coordinate(bufferPiece.getCoordinates().getLetter() + 1, bufferPiece.getCoordinates().getNumber() - 1))) {
                        viewModel.makeEnPassant(bufferPiece, currentCoordinate);
                        move = new Move(bufferPiece, "en passant", new Piece(bufferPiece.getName(), currentCoordinate, bufferPiece.getColorOfPiece(), false));
                    }

                    //ОБЫЧНЫЙ ХОД
                    else {
                        Piece gotoPiece = new Piece(bufferPiece.getName(), currentCoordinate, bufferPiece.getColorOfPiece(), false);
                        viewModel.makeMove(bufferPiece, gotoPiece);
                        move = new Move(bufferPiece, "move", gotoPiece);
                    }

                    viewModel.afterMove();
                    possibleMovesList.clear();
                    viewModel.sendMoveToFirebase(move);
                }

                //ЕСЛИ ЗДЕСЬ НЕТ ФИГУР
                if (!viewModel.isPieceAtCoordinates(currentCoordinate) && !canMoveThere(currentCoordinate)) {
                    popupWindow.dismiss();
                    possibleMovesList.clear();
                }
                invalidate();
            }
        }
        return true;
    }

    public void makePromotionMove(Piece sacrifacePiece, Piece promotionPiece) {
        viewModel.makePawnPromotion(bufferPiece, sacrifacePiece, promotionPiece);
        Move move = new Move(bufferPiece, "promotion", sacrifacePiece, promotionPiece);
        viewModel.afterMove();
        possibleMovesList.clear();
        viewModel.sendMoveToFirebase(move);
        popupWindow.dismiss();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawARGB(80, 0, 0, 255);                //заполняет весь холст цветом
        drawChessBoard(canvas);                             //отрисовка доски
        drawCheckedSquares(canvas);
        drawMarkup(canvas);
        drawPieces(canvas, pieces);                                 //отрисовка фигур
        drawPossibleMoves(canvas, possibleMovesList);     //отрисовка доступных ходов

        /*drawTakenWhitePieces(canvas);
        drawTakenBlackPieces(canvas);
        drawTakenPieces(canvas, takenPiecesWhite);
        drawTakenPieces(canvas, takenPiecesBlack);*/

    }

    private void drawCheckedSquares(Canvas canvas) {
        Coordinate coordinate = null;
        if (isCheckTheWhite) {
            coordinate = getCoordinateByName("King White");
        } else if (isCheckTheBlack) {
            coordinate = getCoordinateByName("King Black");
        }
        if (coordinate != null) {
            RectF rectF = squares.get(coordinate);
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#D40237"));
            if (rectF != null) {
                canvas.drawRect(rectF, paint);
            }

        }
    }

    private Coordinate getCoordinateByName(String name) {
        for (Piece piece : pieces) {
            if (piece.getName().equals(name)) {
                return piece.getCoordinates();
            }
        }
        return null;
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

    private void drawPieces(Canvas canvas, HashSet<Piece> pieces) {            //рисует все фигуры из ArrayList<Piece> pieces
        for (Piece i : pieces) {
            RectF rectF = squares.get(i.getCoordinates());
            if (rectF != null) {
                drawPieceAt(canvas, listOfPiecesAndPNG.get(i.getName()), reduceThePiece(rectF));
            }
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

                if (!isInEditMode() && viewModel.isInvertedBoard()) {
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
            if (rect != null) {
                canvas.drawText(text[i], rect.left + (float) squareSide / 16, rect.bottom - (float) squareSide / 16, paint);
            }
            white_black_flag = !white_black_flag;
        }
        for (int i = 0; i < 8; i++) {
            if (white_black_flag) {
                paint.setColor(Color.parseColor(colors.get("Первый цвет клетки")));
            } else {
                paint.setColor(Color.parseColor(colors.get("Второй цвет клетки")));
            }
            RectF rect = squares.get(new Coordinate(1, i + 1));
            if (rect != null) {
                canvas.drawText(String.valueOf(i + 1), rect.left + (float) squareSide / 16, rect.top + (float) squareSide / 4, paint);
            }
            white_black_flag = !white_black_flag;
        }
    }


    private Piece getPieceAtCoordinates(Coordinate coordinate, HashSet<Piece> pieces) {             //возвращает фигуру по указанным координатам
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
        for (Coordinate coor : coordinates) {
            RectF rect = squares.get(coor);
            if (rect != null) {
                canvas.drawCircle(rect.centerX(), rect.centerY(), radius0fFreeMovePoint, paint);
            }
        }
    }

    private boolean canMoveThere(Coordinate coordinate) { //проверяет, может ли фигура пойти на указанные координаты
        return possibleMovesList.contains(coordinate);
    }

    /*private boolean checkBordersOfView(float x, float y) {           //проверка не выходит ли указатель за рамки view при перемещении фигуры
        return x < Board.getLeft() || x > Board.getRight() || y < Board.getTop() || y > Board.getBottom();
    }*/

    private Coordinate getCoordinatesByXY(float coorX, float coorY) { //возвращает координаты, инвертированные или нет, в зав. от настроек
        initialisationOfXY();
        if (!isInEditMode() && viewModel.isInvertedBoard()) {
            /*ИНВЕРТИРОВАННАЯ */
            return new Coordinate((int) (9 - (coorX - defX) / squareSide), 1 + ((int) (coorY - defY) / squareSide));
        } else {
            /* ОБЫЧНАЯ */
            return new Coordinate(1 + ((int) (coorX - defX) / squareSide), (int) (9 - (coorY - defY) / squareSide));
        }
    }


}
