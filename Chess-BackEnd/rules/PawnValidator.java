package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.stereotype.Component;

@Component("p")
public class PawnValidator implements MoveValidator{

    private final BoardUtils boardUtils = new BoardUtils();

    @Override
    public boolean isValid(Move move, GameState gameState) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to   = boardUtils.toBoardIndex(move.getTo());
        char[][] board = gameState.getBoard();

        int fromX = from[0] ,fromY = from[1];
        int toX = to[0], toY = to[1];
        char pieceAtFrom = boardUtils.getPiece(board,fromX,fromY);
        if(((pieceAtFrom != 'p') && (pieceAtFrom != 'P')))return false;
        char pieceAtTo = boardUtils.getPiece(board,toX,toY);

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
            if (boardUtils.getPiece(board,midRow,fromY) == ' ' && pieceAtTo == ' ') {
                return true;
            }
        }

        //diagonal capturing of opponent piece
        if (Math.abs(fromY - toY) == 1 && toX == fromX + direction) {
            // Normal diagonal capture
            if (boardUtils.isOpponentPiece(pieceAtTo, color)) {
                return true;
            }
            // En passant capture
            String enPassantTarget = gameState.getEnPassantTargetSquare();
            if (enPassantTarget != null) {
                int[] enPassantTargetCoords = boardUtils.toBoardIndex(enPassantTarget);
                if (toX == enPassantTargetCoords[0] && toY == enPassantTargetCoords[1]) {
                    // Check if the captured piece is indeed an opponent's pawn at the correct square
                    char capturedPawn = boardUtils.getPiece(board, fromX, toY);
                    return boardUtils.isOpponentPiece(capturedPawn, color) &&
                           Character.toLowerCase(capturedPawn) == 'p';
                }
            }
        }

        return false;
    }
}
