package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.stereotype.Component;


@Component("k")
public class KingValidator implements MoveValidator {

    private final BoardUtils boardUtils = new BoardUtils();
    private final CastlingValidator castlingValidator = new CastlingValidator();

    @Override
    public boolean isValid(Move move, GameState gameState) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());
        char[][] board = gameState.getBoard();

        int fromX = from[0], fromY = from[1];
        int toX = to[0], toY = to[1];

        char pieceAtFrom = boardUtils.getPiece(board, fromX, fromY);
        if (pieceAtFrom != 'k' && pieceAtFrom != 'K') return false;

        PieceColor color = move.getPieceColor();
        char pieceAtTo = boardUtils.getPiece(board, toX, toY);
        if(fromX == toX && Math.abs(fromY-toY) == 2){
            // a null is passed for validation finder for now
            return castlingValidator.isCastling(fromX, fromY, toY, color, gameState, null);
        }

        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;

        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);

        return dx <= 1 && dy <= 1;
    }
}

