package com.example.chessandroid.ViewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.chessandroid.ChessGame.Coordinate;
import com.example.chessandroid.ChessGame.Move;
import com.example.chessandroid.ChessGame.Piece;
import com.example.chessandroid.Model.Callback;
import com.example.chessandroid.Model.FirebaseRepository;
import com.example.chessandroid.Model.FirebaseRepositoryImpl;
import com.example.chessandroid.Model.User;
import com.example.chessandroid.enums.ColorOfPiece;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;

public class ChessViewModel extends ViewModel {

    final private String TAG = "MainActivity";

//ПЕРЕМЕННЫЕ


    private boolean isWhiteMoving = true;       //проверка на очередность ходов
    private boolean isCheckMateWhite;         //флаг мата
    private boolean isCheckMateBlack;         //флаг мата
    private boolean isCheckTheWhite;         //шах белому королю
    private boolean isCheckTheBlack;         //шах черному королю

    //МАССИВЫ И ЛИСТЫ
    ArrayList<Piece> takenPiecesWhite = new ArrayList<>();
    ArrayList<Piece> takenPiecesBlack = new ArrayList<>();

    public LinkedList<Move> movesBuffer = new LinkedList<>();

    private final FirebaseRepository repository = new FirebaseRepositoryImpl();


    private final MutableLiveData<HashSet<Piece>> pieces = new MutableLiveData<>(repository.getPieces());
    private final MutableLiveData<Move> lastMove = new MutableLiveData<>();
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Task<AuthResult>> logInResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWhiteMovingNow = new MutableLiveData<>(this.isWhiteMoving);
    private final MutableLiveData<Boolean> isCheckTheWhiteNow = new MutableLiveData<>(this.isCheckTheWhite);
    private final MutableLiveData<Boolean> isCheckTheBlackNow = new MutableLiveData<>(this.isCheckTheBlack);
    private final MutableLiveData<Boolean> isCheckmateWhiteNow = new MutableLiveData<>(this.isCheckMateWhite);
    private final MutableLiveData<Boolean> isCheckmateBlackNow = new MutableLiveData<>(this.isCheckMateBlack);
    private final MutableLiveData<Boolean> isStalemateNow = new MutableLiveData<>();


    public void loadMove() {
        repository.receiveMove(lastMove::postValue);
    }

    public LiveData<Move> receiveMove() {
        return lastMove;
    }

    public void logIn(String email, String password) {
        repository.logIn(email, password, logInResult::postValue);
    }

    public LiveData<Task<AuthResult>> logInResult() {
        return logInResult;
    }

    public void sighUp(User user, String email, String password) {
        repository.sighUp(user, email, password, logInResult::postValue);
    }

    public LiveData<Task<AuthResult>> sighUpResult() {
        return logInResult;
    }

    public FirebaseUser getCurrentUser() {
        return repository.getCurrentUser();
    }

    public LiveData<User> getUserData() {
        repository.getUserData(currentUser::postValue);
        return currentUser;
    }

    public void isWhiteMoving() {
        if (isMyTurn()) {
            isWhiteMovingNow.postValue(true);
        } else {
            isWhiteMovingNow.postValue(false);
        }
    }

    public void setMyColorOfPiece(String myColorOfPieces) {
        repository.setMyColorOfPieces(myColorOfPieces);
    }

    public void setIsInvertedBoard(boolean isInvertedBoard) {
        repository.setIsInvertedBoard(isInvertedBoard);
    }

    public void setGameID(String gameID) {
        repository.setGameID(gameID);
    }

    public LiveData<HashSet<Piece>> getPieces() {
        return pieces;
    }

    public LiveData<Boolean> isMyTurnNow() {
        return isWhiteMovingNow;
    }

    public LiveData<Boolean> isCheckTheBlackNow() {
        return isCheckTheBlackNow;
    }

    public LiveData<Boolean> isCheckTheWhiteNow() {
        return isCheckTheWhiteNow;
    }

    public LiveData<Boolean> isCheckMateWhiteNow(){return isCheckmateWhiteNow;}
    public LiveData<Boolean> isCheckMateBlackNow(){return isCheckmateBlackNow;}
    public LiveData<Boolean> isStalemateNow() {
        return isStalemateNow;
    }

    public void sendMoveToFirebase(Move move) {
        repository.sendLastMove(move);
    }

    public String myColorOfPiece() {
        return repository.myColorOfPiece();
    }

    public boolean isInvertedBoard() {
        return repository.isInvertedBoard();
    }


    public void makeMove(Piece fromPiece, Piece gotoPiece) {
        repository.removePiece(fromPiece);
        repository.addPiece(gotoPiece);
        pieces.postValue(repository.getPieces());
    }

    public void makeBackMove(Piece fromPiece, Piece gotoPiece) {
        repository.removePiece(gotoPiece);
        repository.addPiece(fromPiece);
        pieces.postValue(repository.getPieces());
    }

    public void eatThePiece(Piece oldAttackPiece, Piece sacrificePiece) {
        Piece newAttackPiece = new Piece(oldAttackPiece.getName(), sacrificePiece.getCoordinates(), oldAttackPiece.getColorOfPiece(), false);
        repository.removePiece(oldAttackPiece);
        repository.removePiece(sacrificePiece);
        repository.addPiece(newAttackPiece);
        pieces.postValue(repository.getPieces());
    }

    public void makeBackEat(Piece oldAttackPiece, Piece sacrificePiece) {
        Piece newAttackPiece = new Piece(oldAttackPiece.getName(), sacrificePiece.getCoordinates(), oldAttackPiece.getColorOfPiece(), false);
        repository.removePiece(newAttackPiece);
        repository.addPiece(oldAttackPiece);
        repository.addPiece(sacrificePiece);
    }


    public boolean isMyTurn() {
        if ((Objects.equals(repository.myColorOfPiece(), "White") && isWhiteMoving) || (Objects.equals(repository.myColorOfPiece(), "Black") && !isWhiteMoving)) {
            return true;
        }
        if ((Objects.equals(repository.myColorOfPiece(), "White") && !isWhiteMoving) || (Objects.equals(repository.myColorOfPiece(), "Black") && isWhiteMoving)) {
        }
        return false;
    }

    public void makePawnPromotion(Piece oldPawn, Piece oldPiece, Piece newPiece) {
        repository.removePiece(oldPawn);
        repository.removePiece(oldPiece);
        repository.addPiece(newPiece);
        pieces.postValue(repository.getPieces());
    }

    public void makeBackMoveByHistory(Move lastAction) {
        // Log.d(TAG, "makeBackMoveByHistory");
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
//                if (sacrificePiece.getColorOfPiece().equals(ColorOfPiece.WHITE)) {
//                    takenPiecesWhite.remove(takenPiecesWhite.size() - 1);
//                } else {
//                    takenPiecesBlack.remove(takenPiecesBlack.size() - 1);
//                }
                break;
        }
        afterMove();
    }

    public void makeForwardMoveByHistory(Move lastAction) {
        // Log.d(TAG, "makeForwardMoveByHistory"+lastAction);
        Piece from = lastAction.getFrom();
        Piece to = lastAction.getTo();
        switch (lastAction.getAction()) {
            case ("move"):
                makeMove(from, to);
                break;

            case ("eat"):
                eatThePiece(from, to);
                break;
            case ("0-0"):
                if (from.getName().equals("King White")) {
                    makeMove(from, new Piece(from.getName(), new Coordinate(7, 1), from.getColorOfPiece(), false));
                    makeMove(new Piece("Rook White", new Coordinate(8, 1), ColorOfPiece.WHITE, true), new Piece("Rook White", new Coordinate(6, 1), ColorOfPiece.WHITE, false));
                }
                if (from.getName().equals("King Black")) {
                    makeMove(from, new Piece(from.getName(), new Coordinate(7, 8), from.getColorOfPiece(), false));
                    makeMove(new Piece("Rook Black", new Coordinate(8, 8), ColorOfPiece.BLACK, true), new Piece("Rook Black", new Coordinate(6, 8), ColorOfPiece.BLACK, false));
                }
                break;
            case ("0-0-0"):
                if (from.getName().equals("King White")) {
                    makeMove(from, new Piece(from.getName(), new Coordinate(3, 1), from.getColorOfPiece(), false));
                    makeMove(new Piece("Rook White", new Coordinate(1, 1), ColorOfPiece.WHITE, true), new Piece("Rook White", new Coordinate(4, 1), ColorOfPiece.WHITE, false));
                }
                if (from.getName().equals("King Black")) {
                    makeMove(from, new Piece(from.getName(), new Coordinate(3, 8), from.getColorOfPiece(), false));
                    makeMove(new Piece("Rook Black", new Coordinate(1, 8), ColorOfPiece.BLACK, true), new Piece("Rook Black", new Coordinate(4, 8), ColorOfPiece.BLACK, false));

                }
                break;
            case ("en passant"):
                makeEnPassant(from, to.getCoordinates());
                break;
            case ("promotion"):
                makePawnPromotion(from, to, lastAction.getPromotionPiece());
                break;
        }
        afterMove();
    }

    public void deleteAllMovesHistory() {
        repository.deleteAllMovesHistory();
    }

    public void castling(Piece piece, Coordinate coordinate) {
        if (piece.getName().equals("King White")) {
            //КОРОТКАЯ РОКИРОВКА БЕЛЫХ
            if (coordinate.equals(new Coordinate(7, 1))) {
                makeMove(piece, new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), false));
                makeMove(new Piece("Rook White", new Coordinate(8, 1), ColorOfPiece.WHITE, true), new Piece("Rook White", new Coordinate(6, 1), ColorOfPiece.WHITE, false));
            }
            //ДЛИННАЯ РОКИРОВКА БЕЛЫХ
            if (coordinate.equals(new Coordinate(3, 1))) {
                makeMove(piece, new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), false));
                makeMove(new Piece("Rook White", new Coordinate(1, 1), ColorOfPiece.WHITE, true), new Piece("Rook White", new Coordinate(4, 1), ColorOfPiece.WHITE, false));
            }
        }
        if (piece.getName().equals("King Black")) {
            //КОРОТКАЯ РОКИРОВКА ЧЕРНЫХ
            if (coordinate.equals(new Coordinate(7, 8))) {
                makeMove(piece, new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), false));
                makeMove(new Piece("Rook Black", new Coordinate(8, 8), ColorOfPiece.BLACK, true), new Piece("Rook Black", new Coordinate(6, 8), ColorOfPiece.BLACK, false));
            }
            //ДЛИННАЯ РОКИРОВКА ЧЕРНЫХ
            if (coordinate.equals(new Coordinate(3, 8))) {
                makeMove(piece, new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), false));
                makeMove(new Piece("Rook Black", new Coordinate(1, 8), ColorOfPiece.BLACK, true), new Piece("Rook Black", new Coordinate(4, 8), ColorOfPiece.BLACK, false));
            }
        }
    }

    public void makeEnPassant(Piece piece, Coordinate coordinate) {
        //ПРИНИМАЕТ В АРГУМЕНТЫ ПЕШКУ, СОВЕРШАЮЩУЮ ВЗЯТИЕ И КООРДИНАТЫ, ГДЕ ОНА ОКАЖЕТСЯ ПОСЛЕ НЕГО
        //ВЗЯТИЕ НА ПРОХОДЕ БЕЛОЙ ПЕШКОЙ
        if (piece.getName().equals("Pawn White")) {
            Piece newAttackPiece = new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), false);
            repository.removePiece(piece);
            repository.removePiece(getPieceAtCoordinates(repository.getPieces(), new Coordinate(coordinate.getLetter(), coordinate.getNumber() - 1)));
            repository.addPiece(newAttackPiece);
            pieces.postValue(repository.getPieces());
        }
        //ВЗЯТИЕ НА ПРОХОДЕ ЧЕРНОЙ ПЕШКОЙ
        if (piece.getName().equals("Pawn Black")) {
            Piece newAttackPiece = new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), false);
            repository.removePiece(piece);
            repository.removePiece(getPieceAtCoordinates(repository.getPieces(), new Coordinate(coordinate.getLetter(), coordinate.getNumber() + 1)));
            repository.addPiece(newAttackPiece);
            pieces.postValue(repository.getPieces());
        }
    }

    public void afterMove() {
        isWhiteMoving = !isWhiteMoving;
        isCheck(repository.getPieces());
        isCheckTheWhiteNow.postValue(isCheckTheWhite);
        isCheckTheBlackNow.postValue(isCheckTheBlack);
        isCheckMate(repository.getPieces());
        isCheckmateWhiteNow.postValue(isCheckMateWhite);
        isCheckmateBlackNow.postValue(isCheckMateBlack);

    }

    private void isCheck(HashSet<Piece> pieces) {
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
        isCheckTheWhite = false;
        isCheckTheBlack = false;


        HashSet<Coordinate> LISTofMOVES = new HashSet<>();
        for (Piece piece : pieces) {
            fillListOfPossibleMoves(piece, LISTofMOVES);
            for (Coordinate coordinate : LISTofMOVES) {
                Piece test = getPieceAtCoordinates(pieces, coordinate);
                //цвет фигуры, которой шах
                if (test.getName().equals("King White")) {
                    isCheckTheWhite = true;

                }
                if (test.getName().equals("King Black")) {
                    isCheckTheBlack = true;
                }
            }
        }
    }

    public boolean isSquareAttacked(Coordinate[] coordinates, HashSet<Piece> pieces) {
        HashSet<Piece> checkingPieces = new HashSet<>();
        if (isWhiteMoving) {
            pieces.stream().filter(x -> x.getColorOfPiece().equals(ColorOfPiece.BLACK)).forEach(checkingPieces::add);
        }
        if (!isWhiteMoving) {
            pieces.stream().filter(x -> x.getColorOfPiece().equals(ColorOfPiece.WHITE)).forEach(checkingPieces::add);
        }
        HashSet<Coordinate> LISTofMOVES = new HashSet<>();
        for (Piece piece : checkingPieces) {
            fillListOfPossibleMoves(piece, LISTofMOVES);
            for (Coordinate coordinate : coordinates) {
                if (LISTofMOVES.contains(coordinate)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void isCheckMate(HashSet<Piece> pieces) {
        /*
        Изначально флаг checkMate делается true.

        1.Взять список всех фигур того цвета, чей сейчас ход.
        2.Взять каждую фигуру и применить к ней метод fillListOfPossibleMoves(), наполнив список доступных ходов
        3.Брать каждый доступный ход и ставить туда фигуру методом move или eat.
        4.Проверять ситуацию на check своему королю и ОЧИЩАТЬ СПИСОК ХОДОВ
        5.Складывать все возможные ходы, позволяющие избежать шаха
        6.Если ходов = 0 - МАТ
        */



        HashSet<Piece> checkingPieces = new HashSet<>();
        HashSet<Coordinate> LISTofMOVES = new HashSet<>();

//тут pieces превращается в поток данных, и фильруется по цвету. потом каждый элемент, прошедший фильтр методом forEach добавляется в коллекцию checkingPieces.
        if (!isWhiteMoving) {
            isCheckMateBlack = true;
            pieces.stream().filter(x -> x.getColorOfPiece().equals(ColorOfPiece.BLACK)).forEach(checkingPieces::add);
        }
        if (isWhiteMoving) {
            isCheckMateWhite = true;
            pieces.stream().filter(x -> x.getColorOfPiece().equals(ColorOfPiece.WHITE)).forEach(checkingPieces::add);
        }
        for (Piece piece : checkingPieces) {
            fillListOfPossibleMoves(piece, LISTofMOVES);
            for (Coordinate coordinate : LISTofMOVES) {
                Piece checkingPieceOrSquare = getPieceAtCoordinates(pieces, coordinate);
                if (checkingPieceOrSquare.getName().equals("Empty Place")) {

                    Piece gotoPiece = new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), piece.isFirstMove());
                    //MakeMove
                    pieces.remove(piece);
                    pieces.add(gotoPiece);
                    //Проверка на шах
                    isCheck(pieces);

                    if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                        isCheckMateBlack = false;
                        //MakeBackMove
                        pieces.remove(gotoPiece);
                        pieces.add(piece);
                        Log.d(TAG, "isCheckMate: "+piece.getColorOfPiece()+" "+coordinate);
                        return;
                    }
                    if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                        isCheckMateWhite = false;
                        //MakeBackMove
                        pieces.remove(gotoPiece);
                        pieces.add(piece);
                        Log.d(TAG, "isCheckMate: "+piece.getColorOfPiece()+" "+coordinate);
                        return;
                    }
                    //MakeBackMove
                    pieces.remove(gotoPiece);
                    pieces.add(piece);
                }
                if (!checkingPieceOrSquare.getName().equals("Empty Place")) {
                    //Eat the Piece
                    Piece newAttackPiece = new Piece(piece.getName(), checkingPieceOrSquare.getCoordinates(), piece.getColorOfPiece(), false);
                    pieces.remove(piece);
                    pieces.remove(checkingPieceOrSquare);
                    pieces.add(newAttackPiece);

                    //Проверка на шах
                    isCheck(pieces);

                    if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                        isCheckMateBlack = false;
                        //Make Back Eat
                        pieces.remove(newAttackPiece);
                        pieces.add(piece);
                        pieces.add(checkingPieceOrSquare);
                        return;
                    }
                    if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                        isCheckMateWhite = false;
                        //Make Back Eat
                        pieces.remove(newAttackPiece);
                        pieces.add(piece);
                        pieces.add(checkingPieceOrSquare);
                        return;
                    }
                    //Make Back Eat
                    pieces.remove(newAttackPiece);
                    pieces.add(piece);
                    pieces.add(checkingPieceOrSquare);
                }
            }
            LISTofMOVES.clear();
        }

        Log.d(TAG, "isCheckMate: "+isCheckMateBlack+" "+isCheckMateWhite);
        if(!isCheckMateWhite||!isCheckMateBlack){
            isCheck(pieces);
            if(!isCheckTheWhite&&!isCheckTheBlack){
                isStalemateNow.postValue(true);
            }
        }



    }

    public boolean isPieceAtCoordinates(Coordinate coordinate) {     //проверяет стоит ли фигура на указанном по координатам квадрате
        for (Piece piece : repository.getPieces()) {
            if (Objects.equals(piece.getCoordinates(), coordinate)) {
                return true;
            }
        }
        return false;
    }

    private Piece getPieceAtCoordinates(HashSet<Piece> pieces, Coordinate coordinate) {             //возвращает фигуру по указанным координатам
        for (Piece piece : pieces) {
            if (Objects.equals(piece.getCoordinates(), (coordinate))) {
                return piece;
            }
        }
        return new Piece("Empty Place", new Coordinate(0, 0), ColorOfPiece.TEST, false);
    }

    //Метод берет заполненный список доступных ходов, прогоняет каждую координату на шах, и оставляет только те ходы, которые не допускают шах.
    public void removeDangerMoves(Piece piece, HashSet<Coordinate> lightedSquares) {
        HashSet<Piece> pieces = repository.getPieces();
        HashSet<Coordinate> LISTofMOVES = new HashSet<>(lightedSquares);
        lightedSquares.clear();

        for (Coordinate coordinate : LISTofMOVES) {
            Piece test = getPieceAtCoordinates(pieces, coordinate);
            if (test.getName().equals("Empty Place")) {
                Piece gotoPiece = new Piece(piece.getName(), coordinate, piece.getColorOfPiece(), piece.isFirstMove());

                //MakeMove
                pieces.remove(piece);
                pieces.add(gotoPiece);
                //Проверка на шах
                isCheck(pieces);


                if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                    lightedSquares.add(coordinate);
                }
                if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                    lightedSquares.add(coordinate);
                }

                //MakeBackMove
                pieces.remove(gotoPiece);
                pieces.add(piece);
            }
            if (!test.getName().equals("Empty Place")) {
                //Eat the Piece
                Piece newAttackPiece = new Piece(piece.getName(), test.getCoordinates(), piece.getColorOfPiece(), false);
                pieces.remove(piece);
                pieces.remove(test);
                pieces.add(newAttackPiece);

                //Cheching
                isCheck(pieces);

                if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK) && !isCheckTheBlack) {
                    lightedSquares.add(coordinate);
                }
                if (piece.getColorOfPiece().equals(ColorOfPiece.WHITE) && !isCheckTheWhite) {
                    lightedSquares.add(coordinate);
                }
                //Make Back Eat
                pieces.remove(newAttackPiece);
                pieces.add(piece);
                pieces.add(test);
            }
        }
    }


    //заносит в указанный лист те клетки, куда может пойти указанная фигура, в зав. от ее имени
    public void fillListOfPossibleMoves(Piece piece, HashSet<Coordinate> list) {
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

                for (int i = 1; i <= 8; i++) {
                    if ((Objects.equals(lastMove.getValue(),
                            new Move(new Piece("Pawn Black", new Coordinate(i, 7), ColorOfPiece.BLACK, true),
                                    "move",
                                    new Piece("Pawn Black", new Coordinate(i, 5), ColorOfPiece.BLACK, false))))
                            && (piece.getCoordinates().equals(new Coordinate(i - 1, 5)) || piece.getCoordinates().equals(new Coordinate(i + 1, 5)))) {
                        list.add(new Coordinate(i, 6));
                    }
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

                for (int i = 1; i <= 8; i++) {
                    if ((Objects.equals(lastMove.getValue(),
                            new Move(new Piece("Pawn White", new Coordinate(i, 2), ColorOfPiece.WHITE, true),
                                    "move",
                                    new Piece("Pawn White", new Coordinate(i, 4), ColorOfPiece.WHITE, false))))
                            && (piece.getCoordinates().equals(new Coordinate(i - 1, 4)) || piece.getCoordinates().equals(new Coordinate(i + 1, 4)))) {
                        list.add(new Coordinate(i, 3));
                    }
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
        //Убирает из списка координат доступных ходов все фигуры одного цвета с выделенной фигурой.

        list.removeIf(coor -> (Objects.equals(getPieceAtCoordinates(repository.getPieces(), coor).getColorOfPiece(), piece.getColorOfPiece())));
        //Убирает все координаты, находящиеся за пределами поля.
        list.removeIf(coor ->
                coor.getLetter() > 8
                        || coor.getLetter() < 1
                        || coor.getNumber() > 8
                        || coor.getNumber() < 1);


    }

    public void addCastling(Piece piece, HashSet<Coordinate> list, HashSet<Piece> pieces) {
        if (piece.getName().equals("King White")) {
            if (piece.isFirstMove()) {
                // ПРОВЕРКА ВОЗМОЖНОСТИ КОРОТКОЙ РОКИРОВКИ БЕЛЫХ 0-0
                if (getPieceAtCoordinates(pieces, new Coordinate(8, 1)).equals(new Piece("Rook White", new Coordinate(8, 1), ColorOfPiece.WHITE, true))) {
                    if ((!isPieceAtCoordinates(new Coordinate(6, 1))) && (!isPieceAtCoordinates(new Coordinate(7, 1)))) {
                        Coordinate[] coordinates = new Coordinate[]{new Coordinate(6, 1), new Coordinate(7, 1)};
                        if (!isSquareAttacked(coordinates, pieces)) {
                            isCheck(pieces);
                            if (!isCheckTheWhite) {
                                list.add(new Coordinate(7, 1));
                            }
                        }
                    }
                }
                // ПРОВЕРКА ВОЗМОЖНОСТИ ДЛИННОЙ РОКИРОВКИ БЕЛЫХ 0-0-0
                if (getPieceAtCoordinates(pieces, new Coordinate(1, 1)).equals(new Piece("Rook White", new Coordinate(1, 1), ColorOfPiece.WHITE, true))) {
                    if ((!isPieceAtCoordinates(new Coordinate(2, 1))) && (!isPieceAtCoordinates(new Coordinate(3, 1))) && (!isPieceAtCoordinates(new Coordinate(4, 1)))) {
                        Coordinate[] coordinates = new Coordinate[]{new Coordinate(2, 1), new Coordinate(3, 1), new Coordinate(4, 1)};
                        if (!isSquareAttacked(coordinates, pieces)) {
                            isCheck(pieces);
                            if (!isCheckTheWhite) {
                                list.add(new Coordinate(3, 1));
                            }
                        }
                    }
                }
            }
        }
        if (piece.getName().equals("King Black")) {
            if (piece.isFirstMove()) {
                // ПРОВЕРКА ВОЗМОЖНОСТИ КОРОТКОЙ РОКИРОВКИ ЧЕРНЫХ 0-0
                if (getPieceAtCoordinates(pieces, new Coordinate(8, 8)).equals(new Piece("Rook Black", new Coordinate(8, 8), ColorOfPiece.BLACK, true))) {
                    if ((!isPieceAtCoordinates(new Coordinate(6, 8))) && (!isPieceAtCoordinates(new Coordinate(7, 8)))) {
                        Coordinate[] coordinates = new Coordinate[]{new Coordinate(6, 8), new Coordinate(7, 8)};
                        if (!isSquareAttacked(coordinates, pieces)) {
                            isCheck(pieces);
                            if (!isCheckTheBlack) {
                                list.add(new Coordinate(7, 8));
                            }
                        }
                    }
                }
                // ПРОВЕРКА ВОЗМОЖНОСТИ ДЛИННОЙ РОКИРОВКИ ЧЕРНЫХ 0-0-0
                if (getPieceAtCoordinates(pieces, new Coordinate(1, 8)).equals(new Piece("Rook Black", new Coordinate(1, 8), ColorOfPiece.BLACK, true))) {
                    if ((!isPieceAtCoordinates(new Coordinate(2, 8))) && (!isPieceAtCoordinates(new Coordinate(3, 8))) && (!isPieceAtCoordinates(new Coordinate(4, 8)))) {
                        Coordinate[] coordinates = new Coordinate[]{new Coordinate(2, 8), new Coordinate(3, 8), new Coordinate(4, 8)};
                        if (!isSquareAttacked(coordinates, pieces)) {
                            isCheck(pieces);
                            if (!isCheckTheBlack) {
                                list.add(new Coordinate(3, 8));
                            }
                        }
                    }
                }
            }
        }
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












/* ArrayList<Piece> takenPiecesWhite = new ArrayList<>();
    ArrayList<Piece> takenPiecesBlack = new ArrayList<>();



    public LinkedList<Move> movesBuffer = new LinkedList<>();



    public void addTakenPiece(Piece piece) {

        if (piece.getColorOfPiece().equals(ColorOfPiece.BLACK)) {

            if (piece.getName().equals("Pawn Black")) {
                for (int i = 1; i < 9; i++) {
                    if (!isTakenPieceAtCoordinates(new Coordinate(9, i))) {
                        takenPiecesBlack.add(new Piece(piece.getName(), new Coordinate(9, i), ColorOfPiece.BLACK, false));
                        break;
                    }
                }
            }
            if (piece.getName().equals("Queen Black")) {
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


    private static void setCountText() {
        int scoreWhite = 0;
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
*/
}
