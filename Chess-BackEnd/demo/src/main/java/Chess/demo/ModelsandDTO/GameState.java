package Chess.demo.ModelsandDTO;

import Chess.demo.Rules.BoardUtils;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Data
public class GameState {

    private BoardUtils boardUtils;
    @Autowired
    public GameState(BoardUtils boardUtils){
        this.boardUtils = boardUtils;
        reset();
    }
    private UUID gameId;
    private char[][] board;
    private boolean isWhiteTurn;
    private List<Move.MoveHistory> moveHistory;

    //castling
    private boolean whiteCastled;
    private boolean blackCastled;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean whiteKingRookMoved;
    private boolean whiteQueenRookMoved;
    private boolean blackKingRookMoved;
    private boolean blackQueenRookMoved;

    //gameInfo
    private UUID whitePlayerId;
    private UUID blackPlayerId;
    private PieceColor result;

    public void reset(){
        this.gameId = UUID.randomUUID();
        boardUtils.resetBoard();
        this.board = boardUtils.getBoard();
        this.isWhiteTurn = true;
        this.moveHistory = new ArrayList<>();

        this.whiteCastled = this.blackCastled = false;
        this.whiteKingMoved = this.blackKingMoved = false;
        this.whiteKingRookMoved = this.whiteQueenRookMoved = false;
        this.blackKingRookMoved = this.blackQueenRookMoved = false;
    }

    public void toggleTurn(){
        isWhiteTurn = !isWhiteTurn;
    }
}
