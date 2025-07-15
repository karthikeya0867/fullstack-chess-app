package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("r")
public class RookValidator implements MoveValidator{

    private  boolean whiteLeftRookMoved = false;
    private  boolean whiteRightRookMoved = false;
    private  boolean blackLeftRookMoved = false;
    private  boolean blackRightRookMoved = false;

    private final BoardUtils boardUtils;
    @Autowired
    public  RookValidator(BoardUtils boardUtils){
        this.boardUtils = boardUtils;
    }

    @Override
    public boolean isValid(Move move) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to   = boardUtils.toBoardIndex((move.getTo()));

        int fromX = from[0] ,fromY = from[1];
        int toX = to[0], toY = to[1];
        char pieceAtFrom = boardUtils.getPiece(fromX,fromY);
        if(((pieceAtFrom != 'r') && (pieceAtFrom != 'R')) && (pieceAtFrom != 'q') && (pieceAtFrom != 'Q'))return false;
        char pieceAtTo = boardUtils.getPiece(toX,toY);

        PieceColor color = move.getPieceColor();

        if (boardUtils.isFriendlyPiece(pieceAtTo, color)) return false;
        if((fromX - toX == 0 || fromY - toY == 0 ) && isPathClear(fromX,fromY,toX,toY)){
            updateRookMoved(fromX, fromY, color);
            return true;
        }
        return false;
    }
    public boolean isPathClear(int fromX,int fromY , int toX , int toY){

        if(fromX == toX){
            int start = Math.min(fromY , toY) + 1;
            int end = Math.max(fromY,toY);
            for(int i = start;i < end;i++){
                if(boardUtils.getPiece(fromX,i) != ' ')return false;
            }
        }else if(fromY == toY){
            int start = Math.min(fromX,toX) + 1;
            int end = Math.max(fromX,toX);
            for(int i = start;i < end;i++){
                if(boardUtils.getPiece(i,fromY) != ' ')return false;
            }
        }

        return true;
    }

    private void updateRookMoved(int x, int y, PieceColor color) {
        if (color == PieceColor.White) {
            if (x == 7 && y == 0) whiteLeftRookMoved = true;
            if (x == 7 && y == 7) whiteRightRookMoved = true;
        } else {
            if (x == 0 && y == 0) blackLeftRookMoved = true;
            if (x == 0 && y == 7) blackRightRookMoved = true;
        }
    }

    public boolean hasRookMoved(int x , int y){
        if (x == 7 && y == 0) return whiteLeftRookMoved;
        if (x == 7 && y == 7) return whiteRightRookMoved;
        if (x == 0 && y == 0) return blackLeftRookMoved;
        if (x == 0 && y == 7) return blackRightRookMoved;
        return true;
    }
}
