package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.GameState;
import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.PieceColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("r")
public class RookValidator implements MoveValidator{


    private final BoardUtils boardUtils;
    private final GameState gameState;
    @Autowired
    public  RookValidator(BoardUtils boardUtils,
                          GameState gameState){
        this.boardUtils = boardUtils;
        this.gameState = gameState;
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
            if (x == 7 && y == 0) gameState.setWhiteQueenRookMoved(true);
            if (x == 7 && y == 7) gameState.setWhiteKingRookMoved(true);
        } else {
            if (x == 0 && y == 0) gameState.setBlackQueenRookMoved(true);
            if (x == 0 && y == 7) gameState.setBlackKingRookMoved(true);
        }
    }

    public boolean hasRookMoved(int x , int y){
        if (x == 7 && y == 0) return gameState.isWhiteQueenRookMoved();
        if (x == 7 && y == 7) return gameState.isWhiteKingRookMoved();
        if (x == 0 && y == 0) return gameState.isBlackQueenRookMoved();
        if (x == 0 && y == 7) return gameState.isBlackKingRookMoved();
        return true;
    }
}
