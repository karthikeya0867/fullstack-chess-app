package Chess.demo.modelsandDTO;

import Chess.demo.rules.BoardUtils;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "game_state")
@Data
public class GameState {

    @Id
    private UUID gameId;
    @Transient // Handled as FEN string for persistence
    private char[][] board;
    @Column(columnDefinition = "TEXT")
    private String boardFen; // For persistence
    @Transient // Handled as JSON string for persistence
    private List<Move.MoveHistory> moveHistory;
    @Column(columnDefinition = "TEXT")
    private String moveHistoryJson; // For persistence
    private boolean isWhiteTurn;

    //castling
    private boolean whiteCastled;
    private boolean blackCastled;
    private boolean whiteKingMoved;
    private boolean blackKingMoved;
    private boolean whiteKingRookMoved;
    private boolean whiteQueenRookMoved;
    private boolean blackKingRookMoved;
    private boolean blackQueenRookMoved;

    //en passant
    private String enPassantTargetSquare;

    //fifty-move rule and threefold repetition
    private int halfMoveClock;
    @Transient // Handled as JSON string for persistence
    private List<String> positionHistory;
    @Column(columnDefinition = "TEXT")
    private String positionHistoryJson; // For persistence


    //gameInfo
    private UUID whitePlayerId;
    private UUID blackPlayerId;
    @Enumerated(EnumType.STRING) // Store enum as String
    private PieceColor result;

    @PostLoad // Called after an entity has been loaded from the database
    public void convertJsonToObjects() {
        // Here you would convert moveHistoryJson and positionHistoryJson back to their List objects
        // and boardFen back to char[][] board. This requires a JSON library (like Jackson)
        // and a method to convert FEN to char[][]. For now, this is a placeholder.
        // This will be implemented in GameService or a dedicated converter.
    }

    @PrePersist @PreUpdate // Called before an entity is persisted or updated
    public void convertObjectsToJson() {
        // Here you would convert List objects and char[][] board to their JSON/FEN string representations.
        // This requires a JSON library (like Jackson) and a method to convert char[][] to FEN.
        // For now, this is a placeholder.
        // This will be implemented in GameService or a dedicated converter.
    }


    public GameState(){
        reset();
    }

    public void reset(){
        this.gameId = UUID.randomUUID();
        this.board = new char[][] {
                {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'},
                {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
                {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
                {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},
        };
        this.boardFen = new BoardUtils().toFenString(this.board, this); // Initial FEN
        this.isWhiteTurn = true;
        this.moveHistory = new ArrayList<>();
        this.moveHistoryJson = "[]"; // Initial empty JSON array
        this.positionHistory = new ArrayList<>();
        this.positionHistoryJson = "[]"; // Initial empty JSON array

        this.whiteCastled = this.blackCastled = false;
        this.whiteKingMoved = this.blackKingMoved = false;
        this.whiteKingRookMoved = this.whiteQueenRookMoved = false;
        this.blackKingRookMoved = this.blackQueenRookMoved = false;
        this.enPassantTargetSquare = null;
        this.halfMoveClock = 0;
    }

    public void toggleTurn(){
        isWhiteTurn = !isWhiteTurn;
    }
}
