package Chess.demo.Service;

import Chess.demo.Exceptions.InvalidChessException;
import Chess.demo.ModelsandDTO.*;
import Chess.demo.Rules.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoveValidationService {
    private final BoardUtils boardUtils;
    private final GameThreatAnalyzer threatAnalyzer;
    private final GameState gameState;
    private final ValidationFinder validationFinder;


    @Autowired
    public MoveValidationService(
            BoardUtils boardUtils,
            GameThreatAnalyzer threatAnalyzer,
            GameState gameState,
            ValidationFinder validationFinder
    ) {
        this.boardUtils = boardUtils;
        this.threatAnalyzer = threatAnalyzer;
        this.gameState = gameState;
        this.validationFinder = validationFinder;
    }


    public MoveValidationResult validateMove(Move move) {
        PieceColor color = move.getPieceColor();
        PieceColor opponentColor = boardUtils.oppositeColor(color);
        String message;

        if (!(color == PieceColor.White) && !(color == PieceColor.Black)){
            throw new InvalidChessException("Invalid color: " + color);
        }

        if ((gameState.isWhiteTurn() && !(color == PieceColor.White)) || (!gameState.isWhiteTurn() && (color == PieceColor.White))) {
            throw new InvalidChessException("Not " + color + "'s turn");
        }

        MoveValidator validator = validationFinder.getValidatorFor(move.getPieceType().getSymbol());
        if (validator == null) {
            throw new InvalidChessException("Unsupported piece type: " + move.getPieceType());
        }

        if (!validator.isValid(move)){
            message = "The Move is Not Valid, " + move.getPieceType() + " can't go from " + move.getFrom() + " to " + move.getTo();
            return new MoveValidationResult(message, null, null, null, false, false);
       }
        int[] to = boardUtils.toBoardIndex(move.getTo());
        char captured = boardUtils.getPiece(to[0], to[1]);

        boardUtils.makeMove(move);
        boolean isOwnKingInCheck = threatAnalyzer.isKingInCheck(color);


        if (isOwnKingInCheck) {
            boardUtils.revertMove(move, captured);
            message = "Be Careful, That move puts Your Own King in Check";
            return new MoveValidationResult(message,null,null,null,false,false);
        }

        gameState.toggleTurn();
        PieceColor inCheck = threatAnalyzer.isKingInCheck(opponentColor) ? opponentColor : null;
        PieceColor checkmate = threatAnalyzer.isCheckMate(opponentColor) ? opponentColor : null;
        PieceColor winner = (checkmate == null) ? null : color;
        boolean stalemate = threatAnalyzer.isStaleMate(opponentColor);
        if(stalemate || (checkmate != null))boardUtils.resetBoard();
        message = "Success";
        return new MoveValidationResult(message,winner,inCheck,checkmate,stalemate,true);
    }
}
