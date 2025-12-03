package Chess.demo.service;

import Chess.demo.exceptions.InvalidChessException;
import Chess.demo.modelsandDTO.*;
import Chess.demo.rules.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MoveValidationService {
    private final ValidationFinder validationFinder;
    private final BoardUtils boardUtils = new BoardUtils();
    private final GameThreatAnalyzer threatAnalyzer;


    @Autowired
    public MoveValidationService(ValidationFinder validationFinder) {
        this.validationFinder = validationFinder;
        this.threatAnalyzer = new GameThreatAnalyzer(validationFinder);
    }


    public MoveValidationResult validateMove(Move move, GameState gameState) {
        PieceColor color = move.getPieceColor();
        PieceColor opponentColor = boardUtils.oppositeColor(color);
        String message;

        if (!(color == PieceColor.White) && !(color == PieceColor.Black)) {
            throw new InvalidChessException("Invalid color: " + color);
        }

        if ((gameState.isWhiteTurn() && !(color == PieceColor.White)) || (!gameState.isWhiteTurn() && (color == PieceColor.White))) {
            throw new InvalidChessException("Not " + color + "'s turn");
        }

        // Clear en passant target square from previous turn
        gameState.setEnPassantTargetSquare(null);
        
        char pieceSymbol = boardUtils.getPiece(gameState.getBoard(), boardUtils.toBoardIndex(move.getFrom())[0], boardUtils.toBoardIndex(move.getFrom())[1]);
        MoveValidator validator = validationFinder.getValidatorFor(pieceSymbol);
        
        if (validator == null) {
            throw new InvalidChessException("Unsupported piece type: " + move.getPieceType());
        }

        if (!validator.isValid(move, gameState)) {
            message = "The Move is Not Valid, " + move.getPieceType() + " can't go from " + move.getFrom() + " to " + move.getTo();
            return new MoveValidationResult(message, null, null, null, false, false);
        }
        
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());
        char captured = boardUtils.getPiece(gameState.getBoard(), to[0], to[1]);
        
        // Handle en passant capture - remove the actual captured pawn
        boolean isEnPassant = false;
        if (Character.toLowerCase(pieceSymbol) == 'p' && Math.abs(from[1] - to[1]) == 1 && captured == ' ') {
            if (gameState.getEnPassantTargetSquare() != null &&
                to[0] == boardUtils.toBoardIndex(gameState.getEnPassantTargetSquare())[0] &&
                to[1] == boardUtils.toBoardIndex(gameState.getEnPassantTargetSquare())[1]) {
                
                int capturedPawnRow = from[0]; // Same row as the moving pawn
                int capturedPawnCol = to[1]; // Same column as the destination of the en passant
                captured = boardUtils.getPiece(gameState.getBoard(), capturedPawnRow, capturedPawnCol);
                boardUtils.setPiece(gameState.getBoard(), capturedPawnRow, capturedPawnCol, ' '); // Remove captured pawn
                isEnPassant = true;
            }
        }


        // Simulate move to check for checks on self
        boardUtils.makeMove(gameState.getBoard(), move);
        boolean isOwnKingInCheck = threatAnalyzer.isKingInCheck(color, gameState);

        if (isOwnKingInCheck) {
            boardUtils.revertMove(gameState.getBoard(), move, captured);
            if(isEnPassant){ // If en passant, restore the captured pawn
                int capturedPawnRow = from[0];
                int capturedPawnCol = to[1];
                boardUtils.setPiece(gameState.getBoard(), capturedPawnRow, capturedPawnCol, captured);
            }
            message = "Be Careful, That move puts Your Own King in Check";
            return new MoveValidationResult(message, null, null, null, false, false);
        }
        
        // Update halfMoveClock
        if (Character.toLowerCase(pieceSymbol) == 'p' || captured != ' ') {
            gameState.setHalfMoveClock(0);
        } else {
            gameState.setHalfMoveClock(gameState.getHalfMoveClock() + 1);
        }

        // Set en passant target square if pawn moved two steps
        if (Character.toLowerCase(pieceSymbol) == 'p' && Math.abs(from[0] - to[0]) == 2) {
            int enPassantRow = (from[0] + to[0]) / 2;
            gameState.setEnPassantTargetSquare(boardUtils.toChessNotation(enPassantRow, from[1]));
        }

        // Update GameState for castling and king/rook moved flags
        updateGameStateFlags(move, gameState, pieceSymbol);
        
        // Add current board state to position history
        gameState.getPositionHistory().add(boardUtils.toFenString(gameState.getBoard(), gameState));


        // Check for promotion
        if ((pieceSymbol == 'p' && to[0] == 0) || (pieceSymbol == 'P' && to[0] == 7)) {
            // Promotion handled by separate request
            message = "Pawn promotion needed";
            return new MoveValidationResult(message, null, null, null, false, false);
        }


        gameState.toggleTurn();
        PieceColor inCheck = threatAnalyzer.isKingInCheck(opponentColor, gameState) ? opponentColor : null;
        boolean checkmate = threatAnalyzer.isCheckMate(opponentColor, gameState);
        boolean stalemate = threatAnalyzer.isStaleMate(opponentColor, gameState);
        boolean fiftyMoveDraw = gameState.getHalfMoveClock() >= 100;
        boolean threefoldRepetitionDraw = Collections.frequency(gameState.getPositionHistory(), boardUtils.toFenString(gameState.getBoard(), gameState)) >= 3;


        PieceColor winner = (checkmate) ? color : null;
        if (checkmate || stalemate || fiftyMoveDraw || threefoldRepetitionDraw) {
            // Game over, handle appropriately (e.g., store result, reset for new game)
            // gameState.reset(); // Don't reset here, handled by GameService
            String drawReason = null;
            if (fiftyMoveDraw) drawReason = "Draw by fifty-move rule";
            else if (threefoldRepetitionDraw) drawReason = "Draw by threefold repetition";
            else if (stalemate) drawReason = "Draw by stalemate";

            message = (winner != null) ? "Checkmate! " + winner + " wins." : drawReason;
            return new MoveValidationResult(message, winner, inCheck, (checkmate ? opponentColor : null), stalemate || fiftyMoveDraw || threefoldRepetitionDraw, true);
        }
        message = "Success";
        return new MoveValidationResult(message, winner, inCheck, (checkmate ? opponentColor : null), stalemate, true);
    }
    
    private void updateGameStateFlags(Move move, GameState gameState, char pieceSymbol) {
        int[] from = boardUtils.toBoardIndex(move.getFrom());
        int[] to = boardUtils.toBoardIndex(move.getTo());
        
        // Update king moved status
        if (Character.toLowerCase(pieceSymbol) == 'k') {
            if (move.getPieceColor() == PieceColor.White) {
                gameState.setWhiteKingMoved(true);
            } else {
                gameState.setBlackKingMoved(true);
            }
            // Handle castling rook movement
            if (Math.abs(from[1] - to[1]) == 2) { // Castling
                int rookFromY, rookToY;
                if (to[1] == 6) { // Kingside castling
                    rookFromY = 7;
                    rookToY = 5;
                } else { // Queenside castling
                    rookFromY = 0;
                    rookToY = 3;
                }
                Move rookMove = new Move();
                rookMove.setFrom(boardUtils.toChessNotation(from[0], rookFromY));
                rookMove.setTo(boardUtils.toChessNotation(from[0], rookToY));
                boardUtils.makeMove(gameState.getBoard(), rookMove);
            }
        }
        
        // Update rook moved status
        if (Character.toLowerCase(pieceSymbol) == 'r') {
            if (move.getPieceColor() == PieceColor.White) {
                if (from[0] == 7 && from[1] == 0) gameState.setWhiteQueenRookMoved(true);
                if (from[0] == 7 && from[1] == 7) gameState.setWhiteKingRookMoved(true);
            } else {
                if (from[0] == 0 && from[1] == 0) gameState.setBlackQueenRookMoved(true);
                if (from[0] == 0 && from[1] == 7) gameState.setBlackKingRookMoved(true);
            }
        }
    }


    public void handlePromotion(PieceType pieceType, GameState gameState, String position) {
        int[] pos = boardUtils.toBoardIndex(position);
        char pieceSymbol;
        if (gameState.isWhiteTurn()) { // This means the *previous* move was white's pawn reaching the end
            pieceSymbol = Character.toUpperCase(pieceType.getSymbol().charAt(0));
        } else { // Previous move was black's pawn
            pieceSymbol = Character.toLowerCase(pieceType.getSymbol().charAt(0));
        }
        boardUtils.setPiece(gameState.getBoard(), pos[0], pos[1], pieceSymbol);
    }
}
