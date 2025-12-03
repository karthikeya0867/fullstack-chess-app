package Chess.demo.rules;

import Chess.demo.exceptions.InvalidChessException;
import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BoardUtils {

    //converts the given position(a1,a2 .. h8) into index in chess board
    public int[] toBoardIndex(String position) {
        int rowIndex = 8 - Character.getNumericValue(position.charAt(1));
        int colIndex = (position.charAt(0) - 'a');

        if(!indexInBounds(rowIndex,colIndex)){
            throw new InvalidChessException("Invalid :Received Position -> " + position + "  Is Not On The ChessBoard");
        }

        return new int[]{rowIndex, colIndex};
    }

    public char getPiece(char[][] board,int x, int y) {
        if (!indexInBounds(x, y)) throw new IllegalArgumentException("Out of bounds");
        return board[x][y];
    }

    public void setPiece(char[][] board,int x, int y, char piece) {
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
    public void makeMove(char[][] board,Move move) {
        int[] from = toBoardIndex(move.getFrom());
        int[] to = toBoardIndex(move.getTo());
        char piece = getPiece(board,from[0], from[1]);
        setPiece(board,to[0], to[1], piece);
        setPiece(board,from[0], from[1], ' ');
    }

    public void revertMove(char[][] board,Move move, char capturedPiece) {
        int[] from = toBoardIndex(move.getFrom());
        int[] to = toBoardIndex(move.getTo());
        char movedPiece = getPiece(board,to[0], to[1]);
        setPiece(board,from[0], from[1], movedPiece);
        setPiece(board,to[0], to[1], capturedPiece);
    }


    //find the king based on color
    public int[] findKing(char[][] board,PieceColor color) {
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

    // Generates a partial FEN string for the board state
    public String toFenString(char[][] board, GameState gameState) {
        StringBuilder fen = new StringBuilder();

        // Piece placement
        for (int i = 0; i < 8; i++) {
            int empty = 0;
            for (int j = 0; j < 8; j++) {
                char piece = board[i][j];
                if (piece == ' ') {
                    empty++;
                } else {
                    if (empty > 0) {
                        fen.append(empty);
                        empty = 0;
                    }
                    fen.append(piece);
                }
            }
            if (empty > 0) {
                fen.append(empty);
            }
            if (i < 7) {
                fen.append('/');
            }
        }

        // Active color
        fen.append(gameState.isWhiteTurn() ? " w" : " b");

        // Castling availability
        StringBuilder castling = new StringBuilder();
        if (!gameState.isWhiteKingMoved()) {
            if (!gameState.isWhiteKingRookMoved()) castling.append("K");
            if (!gameState.isWhiteQueenRookMoved()) castling.append("Q");
        }
        if (!gameState.isBlackKingMoved()) {
            if (!gameState.isBlackKingRookMoved()) castling.append("k");
            if (!gameState.isBlackQueenRookMoved()) castling.append("q");
        }
        if (castling.length() == 0) {
            fen.append(" -");
        } else {
            fen.append(" ").append(castling);
        }

        // En passant target square
        fen.append(" ").append(gameState.getEnPassantTargetSquare() == null ? "-" : gameState.getEnPassantTargetSquare());

        return fen.toString();
    }

    public char[][] fromFenString(String fen) {
        char[][] board = new char[8][8];
        String[] parts = fen.split(" ");
        String boardFen = parts[0];

        int row = 0;
        int col = 0;
        for (char c : boardFen.toCharArray()) {
            if (c == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
            } else {
                board[row][col] = c;
                col++;
            }
        }
        return board;
    }
}
