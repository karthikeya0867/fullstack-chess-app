package Chess.demo.Service;

import Chess.demo.Exceptions.InvalidChessException;
import Chess.demo.ModelsandDTO.Move;
import Chess.demo.ModelsandDTO.MoveValidationResult;
import Chess.demo.ModelsandDTO.PieceColor;
import  Chess.demo.ModelsandDTO.PieceType;
import Chess.demo.Rules.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;



@Service
public class MoveValidationService {
    private final Map<PieceType, MoveValidator> validatorMap = new HashMap<>();
    private final BoardUtils boardUtils;
    private final GameThreatAnalyzer threatAnalyzer;

    @Setter
    private  boolean whiteTurn = true;

    @Autowired
    public MoveValidationService(
            RookValidator rookValidator,
            BishopValidator bishopValidator,
            PawnValidator pawnValidator,
            KingValidator kingValidator,
            KnightValidator knightValidator,
            QueenValidator queenValidator,
            BoardUtils boardUtils,
            GameThreatAnalyzer threatAnalyzer
    ) {
        validatorMap.put(PieceType.ROOK, rookValidator);
        validatorMap.put(PieceType.BISHOP, bishopValidator);
        validatorMap.put(PieceType.PAWN, pawnValidator);
        validatorMap.put(PieceType.KING, kingValidator);
        validatorMap.put(PieceType.KNIGHT, knightValidator);
        validatorMap.put(PieceType.QUEEN, queenValidator);

        this.boardUtils = boardUtils;
        this.threatAnalyzer = threatAnalyzer;
    }

    public MoveValidationResult validateMove(Move move) {
        PieceColor color = move.getPieceColor();
        PieceColor opponentColor = boardUtils.oppositeColor(color);
        String message;

        if (!(color == PieceColor.White) && !(color == PieceColor.Black)){
            throw new InvalidChessException("Invalid color: " + color);
        }

        if ((whiteTurn && !(color == PieceColor.White)) || (!whiteTurn && (color == PieceColor.White))) {
            throw new InvalidChessException("Not " + color + "'s turn");
        }

        MoveValidator validator = validatorMap.get(move.getPieceType());
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

        whiteTurn = !whiteTurn;
        PieceColor inCheck = threatAnalyzer.isKingInCheck(opponentColor) ? opponentColor : null;
        PieceColor checkmate = threatAnalyzer.isCheckMate(opponentColor) ? opponentColor : null;
        PieceColor winner = (checkmate == null) ? null : color;
        boolean stalemate = threatAnalyzer.isStaleMate(opponentColor);
        if(stalemate || (checkmate != null))boardUtils.resetBoard();
        message = "Success";
        return new MoveValidationResult(message,winner,inCheck,checkmate,stalemate,true);
    }
}
