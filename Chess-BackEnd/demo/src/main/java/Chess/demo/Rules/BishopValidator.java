package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("b")
public class BishopValidator implements MoveValidator {

    private final BoardUtils boardUtils;

    @Autowired
    public BishopValidator(BoardUtils boardUtils) {
        this.boardUtils = boardUtils;
    }

    @Override
    public boolean isValid(Move move) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());

        int fromX = from[0], fromY = from[1];
        int toX = to[0], toY = to[1];

        char pieceAtFrom = boardUtils.getPiece(fromX,fromY);
        char pieceAtTo = boardUtils.getPiece(toX,toY);

        if ((pieceAtFrom != 'b') && (pieceAtFrom != 'B')  && (pieceAtFrom != 'q') && (pieceAtFrom != 'Q')) return false;

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
            if (boardUtils.getPiece(x,y) != ' ') return false;
            x += stepX;
            y += stepY;
        }

        return true;
    }
}

