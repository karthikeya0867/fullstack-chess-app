package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.stereotype.Component;

@Component("n")
public class KnightValidator implements MoveValidator {

    private final BoardUtils boardUtils = new BoardUtils();

    @Override
    public boolean isValid(Move move, GameState gameState) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());
        char[][] board = gameState.getBoard();

        int fromX = from[0], fromY = from[1];
        int toX = to[0], toY = to[1];

        char pieceAtFrom = boardUtils.getPiece(board, fromX, fromY);
        char pieceAtTo = boardUtils.getPiece(board, toX, toY);

        if ((pieceAtFrom != 'n') && (pieceAtFrom != 'N')) return false;

        PieceColor color = move.getPieceColor();

        // Prevent capture of friendly piece
        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;

        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);

        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
    }
}

