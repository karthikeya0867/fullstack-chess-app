package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.stereotype.Component;

@Component
public class GameThreatAnalyzer {

    private final ValidationFinder validationFinder;
    private final BoardUtils boardUtils = new BoardUtils();

    public GameThreatAnalyzer(ValidationFinder validationFinder) {
        this.validationFinder = validationFinder;
    }

    public boolean isSquareAttackedBy(int[] square, PieceColor attackerColor, GameState gameState) {
        char[][] board = gameState.getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                char piece = board[x][y];
                if (boardUtils.isFriendlyPiece(piece, attackerColor) || piece == ' ') continue;

                Move pseudoMove = new Move();
                pseudoMove.setFrom(boardUtils.toChessNotation(x, y));
                pseudoMove.setTo(boardUtils.toChessNotation(square[0], square[1]));
                pseudoMove.setPieceColor(attackerColor);

                MoveValidator validator = validationFinder.getValidatorFor(piece);
                if (validator.isValid(pseudoMove, gameState)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isKingInCheck(PieceColor color, GameState gameState) {
        int[] kingPos = boardUtils.findKing(gameState.getBoard(), color);
        return isSquareAttackedBy(kingPos, boardUtils.oppositeColor(color), gameState);
    }

    public boolean isKingInCheck(PieceColor color, int[] kingPos, GameState gameState) {
        return isSquareAttackedBy(kingPos, boardUtils.oppositeColor(color), gameState);
    }

    public boolean isCheckMate(PieceColor color, GameState gameState) {
        return isKingInCheck(color, gameState) && !kingHasSafeSquare(color, gameState) && !playerHasLegalMove(color, gameState);
    }

    public boolean isStaleMate(PieceColor color, GameState gameState) {
        return (!isKingInCheck(color, gameState) && !playerHasLegalMove(color, gameState));
    }

    private boolean playerHasLegalMove(PieceColor color, GameState gameState) {
        char[][] board = gameState.getBoard();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                char piece = boardUtils.getPiece(board, x, y);

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

                        if (!validator.isValid(move, gameState)) continue;

                        char captured = boardUtils.getPiece(board, i, j);
                        boardUtils.makeMove(board, move);
                        boolean kingSafe = !isKingInCheck(color, gameState);
                        boardUtils.revertMove(board, move, captured);

                        if (kingSafe) return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean kingHasSafeSquare(PieceColor color, GameState gameState) {
        int[] kingPos = boardUtils.findKing(gameState.getBoard(), color);
        int x = kingPos[0], y = kingPos[1];
        String from = boardUtils.toChessNotation(x, y);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int newX = x + dx;
                int newY = y + dy;

                if (!boardUtils.indexInBounds(newX, newY)) continue;

                char target = boardUtils.getPiece(gameState.getBoard(), newX, newY);
                if (boardUtils.isFriendlyPiece(target, color)) continue;

                Move move = new Move();
                move.setFrom(from);
                move.setTo(boardUtils.toChessNotation(newX, newY));
                move.setPieceColor(color);

                char captured = boardUtils.getPiece(gameState.getBoard(), newX, newY);

                boardUtils.makeMove(gameState.getBoard(), move);

                boolean stillInCheck = isKingInCheck(color, new int[]{newX, newY}, gameState);

                boardUtils.revertMove(gameState.getBoard(), move, captured);

                if (!stillInCheck) {
                    return true;
                }
            }
        }
        return false;
    }
}
