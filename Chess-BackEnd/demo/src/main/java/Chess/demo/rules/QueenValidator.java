package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceType;
import org.springframework.stereotype.Component;

@Component("q")
public class QueenValidator implements MoveValidator {

    private final RookValidator rookValidator = new RookValidator();
    private final BishopValidator bishopValidator = new BishopValidator();
    private final BoardUtils boardUtils = new BoardUtils();


    @Override
    public boolean isValid(Move move, GameState gameState) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());
        char[][] board = gameState.getBoard();
        int fromX = from[0], fromY = from[1];
        int toX = to[0], toY = to[1];
        char pieceAtFrom = boardUtils.getPiece(board, fromX, fromY);
        if(pieceAtFrom != 'q' && pieceAtFrom != 'Q') return false;

        // Queen combines the moves of a rook and a bishop
        return bishopValidator.isValid(move, gameState) || rookValidator.isValid(move, gameState);
    }
}
