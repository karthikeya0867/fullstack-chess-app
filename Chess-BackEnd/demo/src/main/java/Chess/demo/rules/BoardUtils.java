package Chess.demo.rules;

import Chess.demo.exceptions.InvalidChessException;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class BoardUtils {

    // initial board setup for a new game
   private char[][] board;

   @PostConstruct
   public void initBoard(){
       board = new char[][] {
                {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
                {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
                {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},
        };
   }

    //converts the given position(a1,a2 .. h8) into index in chess board
    public int[] toBoardIndex(String position) {
        int rowIndex = 8 - Character.getNumericValue(position.charAt(1));
        int colIndex = (position.charAt(0) - 'a');

        if(!indexInBounds(rowIndex,colIndex)){
            throw new InvalidChessException("Invalid :Received Position -> " + position + "  Is Not On The ChessBoard");
        }

        return new int[]{rowIndex, colIndex};
    }

    public char getPiece(int x, int y) {
        if (!indexInBounds(x, y)) throw new IllegalArgumentException("Out of bounds");
        return board[x][y];
    }

    public void setPiece(int x, int y, char piece) {
        if (!indexInBounds(x, y)) throw new IllegalArgumentException("Out of bounds");
        board[x][y] = piece;
    }

    // Check if the given index is within the bounds of the chessboard
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean indexInBounds(int x, int y){
        return (x >= 0  && x <= 7 && y >= 0 && y <= 7);
    }

    //to find if piece at target square is opponent or not
    public boolean isOpponentPiece(char piece, PieceColor color) {
        return (color == PieceColor.White && Character.isLowerCase(piece)) ||
                (color == PieceColor.Black && Character.isUpperCase(piece));
    }

    //to find if piece at target square is friendly or not
    public boolean isFriendlyPiece(char piece, PieceColor color) {
        return (color == PieceColor.White && Character.isUpperCase(piece)) ||
                (color == PieceColor.Black && Character.isLowerCase(piece));
    }

    //to make move
    public void makeMove(Move move) {
        int[] from = toBoardIndex(move.getFrom());
        int[] to = toBoardIndex(move.getTo());
        char piece = getPiece(from[0], from[1]);
        setPiece(to[0], to[1], piece);
        setPiece(from[0], from[1], ' ');
    }

    public void revertMove(Move move, char capturedPiece) {
        int[] from = toBoardIndex(move.getFrom());
        int[] to = toBoardIndex(move.getTo());
        char movedPiece = getPiece(to[0], to[1]);
        setPiece(from[0], from[1], movedPiece);
        setPiece(to[0], to[1], capturedPiece);
    }


    //find the king based on color
    public int[] findKing(PieceColor color) {
        char kingChar = (color == PieceColor.White) ? 'K' : 'k';
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == kingChar) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalStateException("King not found for " + color);
    }


    public PieceColor oppositeColor(PieceColor color){
        return (color == PieceColor.White) ? PieceColor.Black : PieceColor.White ;
    }

    public String toChessNotation(int row, int col) {
        return "" + (char)('a' + col) + (8 - row);
    }

    public void resetBoard(){
       board = new char[][] {
               {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
               {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
               {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
               {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
               {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
               {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
               {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
               {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},
       };
    }


}
