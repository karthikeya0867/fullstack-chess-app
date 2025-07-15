package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("p")
public class PawnValidator implements MoveValidator{

    private final BoardUtils boardUtils;
    @Autowired
    public  PawnValidator(BoardUtils boardUtils){
        this.boardUtils = boardUtils;
    }

    @Override
    public boolean isValid(Move move) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to   = boardUtils.toBoardIndex(move.getTo());

        int fromX = from[0] ,fromY = from[1];
        int toX = to[0], toY = to[1];
        char pieceAtFrom = boardUtils.getPiece(fromX,fromY);
        if(((pieceAtFrom != 'p') && (pieceAtFrom != 'P')))return false;
        char pieceAtTo = boardUtils.getPiece(toX,toY);

        PieceColor color = move.getPieceColor();
        //forward (+1) if white pieces and (-1) for black
        int direction = (color == PieceColor.White) ? -1 : 1;
        //starting rows of white and black pawns
        int startRow = (color == PieceColor.White) ? 6 : 1;

        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;

        //simple forward move by one step
        if (fromY == toY && toX == fromX + direction && pieceAtTo == ' ') {
            return true;
        }
        //2 steps if pawn is in starting block
        if (fromY == toY && fromX == startRow && toX == fromX + 2 * direction) {
            //checking 1st square is empty or occupied
            int midRow = fromX + direction;
            if (boardUtils.getPiece(midRow,fromY) == ' ' && pieceAtTo == ' ') {
                return true;
            }
        }

        //diagonal capturing of opponent piece
        if (Math.abs(fromY - toY) == 1 && toX == fromX + direction) {
            return boardUtils.isOpponentPiece(pieceAtTo, color);
        }

        return false;
    }
}
