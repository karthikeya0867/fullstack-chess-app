package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.PieceColor;
import org.springframework.stereotype.Component;

@Component
public class CastlingValidator {

    public boolean isCastling(int fromX, int fromY, int toY, PieceColor color, GameState gameState, ValidationFinder validationFinder) {
        GameThreatAnalyzer threatAnalyzer = new GameThreatAnalyzer(validationFinder);
        BoardUtils boardUtils = new BoardUtils();

        if (color == PieceColor.White && gameState.isWhiteKingMoved()) return false;
        if (color == PieceColor.Black && gameState.isBlackKingMoved()) return false;

        // Check if king is currently in check
        if (threatAnalyzer.isKingInCheck(color, gameState)) return false;

        if (toY > fromY) { // Kingside castling
            int rookY = 7;
            char rook = boardUtils.getPiece(gameState.getBoard(), fromX, rookY);
            
            boolean rookHasMoved = (color == PieceColor.White) ? gameState.isWhiteKingRookMoved() : gameState.isBlackKingRookMoved();
            if (rookHasMoved) return false;

            if (color == PieceColor.White && rook != 'R') return false;
            if (color == PieceColor.Black && rook != 'r') return false;

            // Check if squares between king and rook are empty and not under attack
            for (int y = fromY + 1; y < rookY; y++) {
                if (boardUtils.getPiece(gameState.getBoard(), fromX, y) != ' ' || threatAnalyzer.isSquareAttackedBy(new int[]{fromX, y}, boardUtils.oppositeColor(color), gameState))
                    return false;
            }
            
            return true;
        }

        if (toY < fromY) { // Queenside castling
            int rookY = 0;
            char rook = boardUtils.getPiece(gameState.getBoard(), fromX, rookY);

            boolean rookHasMoved = (color == PieceColor.White) ? gameState.isWhiteQueenRookMoved() : gameState.isBlackQueenRookMoved();
            if (rookHasMoved) return false;

            if (color == PieceColor.White && rook != 'R') return false;
            if (color == PieceColor.Black && rook != 'r') return false;

            // Check if squares between king and rook are empty and not under attack
            for (int y = rookY + 1; y < fromY; y++) {
                if (boardUtils.getPiece(gameState.getBoard(), fromX, y) != ' ' || threatAnalyzer.isSquareAttackedBy(new int[]{fromX, y}, boardUtils.oppositeColor(color), gameState))
                    return false;
            }
            
            return true;
        }

        return false;
    }
}