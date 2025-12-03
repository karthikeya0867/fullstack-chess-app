package Chess.demo.service;

import Chess.demo.GameStateRepository;
import Chess.demo.exceptions.InvalidChessException;
import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.MoveValidationResult;
import Chess.demo.modelsandDTO.PieceColor;
import Chess.demo.modelsandDTO.PieceType;
import Chess.demo.rules.BoardUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final GameStateRepository gameStateRepository;
    private final MoveValidationService moveValidationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BoardUtils boardUtils = new BoardUtils();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public GameService(GameStateRepository gameStateRepository, MoveValidationService moveValidationService, SimpMessagingTemplate messagingTemplate) {
        this.gameStateRepository = gameStateRepository;
        this.moveValidationService = moveValidationService;
        this.messagingTemplate = messagingTemplate;
    }

    public GameState createNewGame(UUID userId) {
        GameState newGame = new GameState();
        newGame.setWhitePlayerId(userId);
        updatePersistedFields(newGame);
        GameState savedGame = gameStateRepository.save(newGame);
        messagingTemplate.convertAndSend("/topic/games/open", getOpenGames()); // Notify about new open game
        return savedGame;
    }

    public GameState joinGame(UUID gameId, UUID userId) {
        GameState gameState = getGameState(gameId);
        if (gameState.getBlackPlayerId() != null) {
            throw new InvalidChessException("Game " + gameId + " is already full.");
        }
        gameState.setBlackPlayerId(userId);
        updatePersistedFields(gameState);
        GameState updatedGame = gameStateRepository.save(gameState);
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame); // Notify game participants
        messagingTemplate.convertAndSend("/topic/games/open", getOpenGames()); // Update open games list
        return updatedGame;
    }

    public List<GameState> getOpenGames() {
        return gameStateRepository.findAll().stream()
                .filter(gameState -> gameState.getBlackPlayerId() == null)
                .collect(Collectors.toList());
    }

    public GameState getGameState(UUID gameId) {
        Optional<GameState> gameStateOptional = gameStateRepository.findById(gameId);
        if (gameStateOptional.isPresent()) {
            GameState gameState = gameStateOptional.get();
            hydrateGameState(gameState);
            return gameState;
        }
        throw new InvalidChessException("Game not found with ID: " + gameId);
    }

    public GameState resetGame(UUID gameId) {
        GameState gameState = getGameState(gameId);
        gameState.reset();
        updatePersistedFields(gameState);
        GameState updatedGame = gameStateRepository.save(gameState);
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame); // Notify game participants
        messagingTemplate.convertAndSend("/topic/games/open", getOpenGames()); // Update open games list
        return updatedGame;
    }

    public MoveValidationResult makeMove(UUID gameId, Move move, UUID performingUserId) {
        GameState gameState = getGameState(gameId);
        
        // Check if the user making the move is a player in this game
        if (!performingUserId.equals(gameState.getWhitePlayerId()) && !performingUserId.equals(gameState.getBlackPlayerId())) {
            throw new InvalidChessException("User " + performingUserId + " is not a player in game " + gameId);
        }

        // Check if it's the performing user's turn
        if (gameState.isWhiteTurn() && !performingUserId.equals(gameState.getWhitePlayerId())) {
            throw new InvalidChessException("It's White's turn, but user " + performingUserId + " is Black.");
        }
        if (!gameState.isWhiteTurn() && !performingUserId.equals(gameState.getBlackPlayerId())) {
            throw new InvalidChessException("It's Black's turn, but user " + performingUserId + " is White.");
        }
        
        MoveValidationResult validationResult = moveValidationService.validateMove(move, gameState);
        
        if (validationResult.isValid()) {
            // Update move history
            Move.MoveHistory moveHistoryEntry = new Move.MoveHistory();
            moveHistoryEntry.setFrom(move.getFrom());
            moveHistoryEntry.setTo(move.getTo());
            moveHistoryEntry.setPieceMoved(boardUtils.getPiece(gameState.getBoard(), boardUtils.toBoardIndex(move.getFrom())[0], boardUtils.toBoardIndex(move.getFrom())[1]));
            // Assuming capture logic is handled in moveValidationService.validateMove and board is updated
            // For now, let's just add the move itself
            gameState.getMoveHistory().add(moveHistoryEntry);


            updatePersistedFields(gameState);
            GameState updatedGame = gameStateRepository.save(gameState);
            messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame); // Notify game participants
        }
        return validationResult;
    }

    public void handlePromotion(PieceType pieceType, UUID gameId, String position, UUID performingUserId) {
        GameState gameState = getGameState(gameId);
        
        // Check if the user making the promotion is the correct player
        if (gameState.isWhiteTurn() && !performingUserId.equals(gameState.getBlackPlayerId())) { // Promotion happens after a successful move, so turn already toggled
            throw new InvalidChessException("It's Black's turn, but user " + performingUserId + " is White.");
        }
        if (!gameState.isWhiteTurn() && !performingUserId.equals(gameState.getWhitePlayerId())) {
            throw new InvalidChessException("It's White's turn, but user " + performingUserId + " is Black.");
        }

        moveValidationService.handlePromotion(pieceType, gameState, position);
        updatePersistedFields(gameState);
        GameState updatedGame = gameStateRepository.save(gameState);
        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/state", updatedGame); // Notify game participants
    }


    private void hydrateGameState(GameState gameState) {
        // Convert FEN to char[][] board
        gameState.setBoard(boardUtils.fromFenString(gameState.getBoardFen()));

        // Convert JSON to List<Move.MoveHistory>
        try {
            CollectionType moveHistoryType = objectMapper.getTypeFactory().constructCollectionType(List.class, Move.MoveHistory.class);
            gameState.setMoveHistory(objectMapper.readValue(gameState.getMoveHistoryJson(), moveHistoryType));
        } catch (JsonProcessingException e) {
            gameState.setMoveHistory(new ArrayList<>()); // Fallback to empty list
            // Log error
        }

        // Convert JSON to List<String> for positionHistory
        try {
            CollectionType positionHistoryType = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
            gameState.setPositionHistory(objectMapper.readValue(gameState.getPositionHistoryJson(), positionHistoryType));
        } catch (JsonProcessingException e) {
            gameState.setPositionHistory(new ArrayList<>()); // Fallback to empty list
            // Log error
        }
    }

    private void updatePersistedFields(GameState gameState) {
        // Convert char[][] board to FEN
        gameState.setBoardFen(boardUtils.toFenString(gameState.getBoard(), gameState));

        // Convert List<Move.MoveHistory> to JSON
        try {
            gameState.setMoveHistoryJson(objectMapper.writeValueAsString(gameState.getMoveHistory()));
        } catch (JsonProcessingException e) {
            gameState.setMoveHistoryJson("[]"); // Fallback to empty JSON array
            // Log error
        }

        // Convert List<String> for positionHistory to JSON
        try {
            gameState.setPositionHistoryJson(objectMapper.writeValueAsString(gameState.getPositionHistory()));
        } catch (JsonProcessingException e) {
            gameState.setPositionHistoryJson("[]"); // Fallback to empty JSON array
            // Log error
        }
    }
}
