package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("k")
public class KingValidator implements MoveValidator {

    private final BoardUtils boardUtils;
    private final CastlingValidator castlingValidator;
    private final GameState gameState;
    @Autowired
    public KingValidator(BoardUtils boardUtils,
                         CastlingValidator castlingValidator,
                         GameState gameState) {

        this.boardUtils = boardUtils;
        this.castlingValidator = castlingValidator;
        this.gameState = gameState;
    }

    @Override
    public boolean isValid(Move move) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());

        int fromX = from[0], fromY = from[1];
        int toX = to[0], toY = to[1];

        char pieceAtFrom = boardUtils.getPiece(fromX, fromY);
        if (pieceAtFrom != 'k' && pieceAtFrom != 'K') return false;

        PieceColor color = move.getPieceColor();
        char pieceAtTo = boardUtils.getPiece(toX, toY);
        if(fromX == toX && Math.abs(fromY-toY) == 2){

            if(color == PieceColor.White ? !gameState.isWhiteCastled() : !gameState.isBlackCastled() && castlingValidator.isCastling(fromX , fromY, toY , color)){
                if(color == PieceColor.White)gameState.setWhiteCastled(true);
                else gameState.setBlackCastled(true);
                return true;
            }
        }

        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;

        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);

        if(dx <= 1 && dy <= 1){
            if(color == PieceColor.White)gameState.setWhiteKingMoved(true);
            else gameState.setBlackKingMoved(true);
            return true;
        }
        return false;
    }
}

