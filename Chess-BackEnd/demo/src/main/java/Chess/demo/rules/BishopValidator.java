package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.stereotype.Component;

@Component("b")
public class BishopValidator implements MoveValidator {

    private final BoardUtils boardUtils = new BoardUtils();

    @Override
    public boolean isValid(Move move, GameState gameState) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());
        char[][] board = gameState.getBoard();

        int fromX = from[0], fromY = from[1];
        int toX = to[0], toY = to[1];

        char pieceAtTo = boardUtils.getPiece(board, toX,toY);

        PieceColor color = move.getPieceColor();
        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;

        int dx = toX - fromX;
        int dy = toY - fromY;

        if (Math.abs(dx) != Math.abs(dy)) return false;

        int stepX = Integer.signum(dx);
        int stepY = Integer.signum(dy);
        int x = fromX + stepX;
        int y = fromY + stepY;

        while (x != toX && y != toY) {
            if (boardUtils.getPiece(board, x,y) != ' ') return false;
            x += stepX;
            y += stepY;
        }

        return true;
    }
}

