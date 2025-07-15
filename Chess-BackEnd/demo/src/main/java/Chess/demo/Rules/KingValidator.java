package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("k")
public class KingValidator implements MoveValidator {

    private final BoardUtils boardUtils;
    private final CastlingValidator castlingValidator;

    @Autowired
    public KingValidator(BoardUtils boardUtils, CastlingValidator castlingValidator) {

        this.boardUtils = boardUtils;
        this.castlingValidator = castlingValidator;
    }
    private boolean whiteCastled = false;
    private boolean blackCastled = false;
    private boolean whiteKingHasMoved = false;
    private boolean blackKingHasMoved = false;

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

            if(color == PieceColor.White ? !whiteCastled : !blackCastled && castlingValidator.isCastling(fromX , fromY, toY , color,whiteKingHasMoved,blackKingHasMoved)){
                if(color == PieceColor.White)whiteCastled = true;
                else blackCastled = true;
                return true;
            }
        }

        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;

        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);

        if(dx <= 1 && dy <= 1){
            if(color == PieceColor.White)whiteKingHasMoved = true;
            else blackKingHasMoved = true;
            return true;
        }
        return false;
    }
}

