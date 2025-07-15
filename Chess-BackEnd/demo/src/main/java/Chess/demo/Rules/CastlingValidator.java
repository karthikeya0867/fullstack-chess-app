package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import Chess.demo.ModelsandDTO.PieceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CastlingValidator {


    private final GameThreatAnalyzer threatAnalyzer;
    private final BoardUtils boardUtils;
    private final RookValidator rookValidator;
    @Autowired
    public CastlingValidator(BoardUtils boardUtils ,
                             RookValidator rookValidator,
                             @Lazy GameThreatAnalyzer threatAnalyzer){
        this.boardUtils = boardUtils;
        this.rookValidator = rookValidator;
        this.threatAnalyzer = threatAnalyzer;
    }

    public boolean isCastling(int fromX, int fromY, int toY, PieceColor color , boolean whiteKingHasMoved , boolean blackKingHasMoved) {

        if (color == PieceColor.White && whiteKingHasMoved) return false;
        if (color == PieceColor.Black && blackKingHasMoved) return false;

        if (toY > fromY) {
            int rookY = 7;
            char rook = boardUtils.getPiece(fromX, rookY);
            boolean rookHasMoved = rookValidator.hasRookMoved(fromX, rookY);

            if (color == PieceColor.White && (rook != 'R' || rookHasMoved)) return false;
            if (color == PieceColor.Black && (rook != 'r' || rookHasMoved)) return false;

            for (int y = fromY + 1; y < rookY; y++) {
                if (boardUtils.getPiece(fromX, y) != ' ' || threatAnalyzer.isKingInCheck(color,new int[]{fromX,y})) return false;
            }
            String fromPos = boardUtils.toChessNotation(fromX,rookY);
            String toPos = boardUtils.toChessNotation(fromX,rookY - 2);
            Move moveRook = new Move(fromPos,toPos, PieceType.ROOK,color);
            boardUtils.makeMove(moveRook);
            return true;
        }

        if (toY < fromY) {
            int rookY = 0;
            char rook = boardUtils.getPiece(fromX, rookY);
            boolean rookHasMoved = rookValidator.hasRookMoved(fromX, rookY);

            if (color == PieceColor.White && (rook != 'R' || rookHasMoved)) return false;
            if (color == PieceColor.Black && (rook != 'r' || rookHasMoved)) return false;

            for (int y = rookY + 1; y < fromY; y++) {
                if (boardUtils.getPiece(fromX, y) != ' ' || threatAnalyzer.isKingInCheck(color,new int[]{fromX,y})) return false;
            }

            String fromPos = boardUtils.toChessNotation(fromX,rookY);
            String toPos = boardUtils.toChessNotation(fromX,rookY + 2);
            Move moveRook = new Move(fromPos,toPos, PieceType.ROOK,color);
            boardUtils.makeMove(moveRook);
            return true;
        }

        return false;
    }

}
