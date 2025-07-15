package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameThreatAnalyzer {


    private final ValidationFinder validationFinder;
    private final BoardUtils boardUtils;
    @Autowired
    public GameThreatAnalyzer(BoardUtils boardUtils,
                              ValidationFinder validationFinder) {
        this.boardUtils = boardUtils;
        this.validationFinder = validationFinder;
    }


    public boolean isKingInCheck(PieceColor color) {
        int[] kingPos = boardUtils.findKing(color);
        return isKingInCheck(color, kingPos);
    }


    public boolean isKingInCheck(PieceColor color , int[] kingPos) {
        char[][] board = boardUtils.getBoard();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                char piece = board[x][y];
                if (boardUtils.isFriendlyPiece(piece, color) || piece == ' ') continue;

                Move pseudoMove = new Move();
                pseudoMove.setFrom(boardUtils.toChessNotation(x, y));
                pseudoMove.setTo(boardUtils.toChessNotation(kingPos[0], kingPos[1]));
                pseudoMove.setPieceColor(boardUtils.oppositeColor(color));

                MoveValidator validator = validationFinder.getValidatorFor(piece);
                if (validator.isValid(pseudoMove)) {
                    return true; // King is under attack
                }
            }
        }

        return false;
    }

    public boolean isCheckMate(PieceColor color){
        return (isKingInCheck(color) && !kingHasSafeSquare(color));
    }

    public boolean isStaleMate(PieceColor color){
        return (!isKingInCheck(color) && !playerHasLegalMove(color));
    }

    private boolean playerHasLegalMove(PieceColor color) {
        char[][] board = boardUtils.getBoard();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                char piece = board[x][y];

                if (boardUtils.isOpponentPiece(piece, color) || piece == ' ') continue;

                String from = boardUtils.toChessNotation(x, y);
                MoveValidator validator = validationFinder.getValidatorFor(piece);

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (x == i && y == j) continue;

                        String to = boardUtils.toChessNotation(i, j);
                        Move move = new Move();
                        move.setFrom(from);
                        move.setTo(to);
                        move.setPieceColor(color);

                        if (!validator.isValid(move)) continue;

                        // Simulate move
                        char captured = boardUtils.getPiece(i, j);
                        boardUtils.makeMove(move);
                        boolean kingSafe = !isKingInCheck(color);
                        boardUtils.revertMove(move, captured);

                        if (kingSafe) return true;
                    }
                }
            }
        }

        return false;
    }


    private boolean kingHasSafeSquare(PieceColor color) {
        int[] kingPos = boardUtils.findKing(color);
        int x = kingPos[0], y = kingPos[1];
        String from = boardUtils.toChessNotation(x, y);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int newX = x + dx;
                int newY = y + dy;

                if (!boardUtils.indexInBounds(newX, newY)) continue;

                char target = boardUtils.getPiece(newX, newY);
                if (boardUtils.isFriendlyPiece(target, color)) continue;

                Move move = new Move();
                move.setFrom(from);
                move.setTo(boardUtils.toChessNotation(newX, newY));
                move.setPieceColor(color);

                // Save the piece at destination for revert
                char captured = boardUtils.getPiece(newX, newY);

                // Make temporary move
                boardUtils.makeMove(move);

                boolean stillInCheck = isKingInCheck(color, new int[]{newX, newY});

                // Revert the move
                boardUtils.revertMove(move, captured);

                if (!stillInCheck) {
                    return true; // Found at least one safe square
                }
            }
        }
        return false; // No legal move that gets out of check
    }


}
