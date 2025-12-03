package Chess.demo.controller;

import Chess.demo.exceptions.InvalidChessException;
import Chess.demo.modelsandDTO.*;
import Chess.demo.service.GameService;
import Chess.demo.service.MoveValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameState> createNewGame() {
        UUID userId = getAuthenticatedUserId();
        GameState newGame = gameService.createNewGame(userId);
        return ResponseEntity.ok(newGame);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameState> joinGame(@PathVariable UUID gameId) {
        UUID userId = getAuthenticatedUserId();
        try {
            GameState joinedGame = gameService.joinGame(gameId, userId);
            return ResponseEntity.ok(joinedGame);
        } catch (InvalidChessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/open")
    public ResponseEntity<List<GameState>> getOpenGames() {
        List<GameState> openGames = gameService.getOpenGames();
        return ResponseEntity.ok(openGames);
    }


    @PostMapping("/{gameId}/move")
    public ResponseEntity<APIResponse<MoveValidationResult>> validateMove(
            @PathVariable UUID gameId,
            @Valid @RequestBody Move move,
            HttpServletRequest request) {

        UUID userId = getAuthenticatedUserId();
        MoveValidationResult result;
        try {
            result = gameService.makeMove(gameId, move, userId);
        } catch (InvalidChessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        String message = result.getMessage();

        APIResponse<MoveValidationResult> response = new APIResponse<>(
                message,
                result.isValid() ? 200 : 400,
                request.getRequestURI(),
                result
        );

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{gameId}/board")
    public String[][] getFrontendBoard(@PathVariable UUID gameId) {
        GameState gameState;
        try {
            gameState = gameService.getGameState(gameId);
        } catch (InvalidChessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found", e);
        }
        
        char[][] currentBoard = gameState.getBoard();

        String[][] board = new String[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char piece = currentBoard[i][j];

                switch (piece) {
                    case 'P': board[i][j] = "w-pawn"; break;
                    case 'R': board[i][j] = "w-rook"; break;
                    case 'N': board[i][j] = "w-knight"; break;
                    case 'B': board[i][j] = "w-bishop"; break;
                    case 'Q': board[i][j] = "w-queen"; break;
                    case 'K': board[i][j] = "w-king"; break;
                    case 'p': board[i][j] = "b-pawn"; break;
                    case 'r': board[i][j] = "b-rook"; break;
                    case 'n': board[i][j] = "b-knight"; break;
                    case 'b': board[i][j] = "b-bishop"; break;
                    case 'q': board[i][j] = "b-queen"; break;
                    case 'k': board[i][j] = "b-king"; break;
                    default: board[i][j] = null; break;
                }
            }
        }

        return board;
    }

    @PostMapping("/{gameId}/reset")
    public String[][] resetGame(@PathVariable UUID gameId) {
        GameState gameState;
        try {
            gameState = gameService.resetGame(gameId);
        } catch (InvalidChessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found", e);
        }
        return getFrontendBoard(gameId);
    }

    @PostMapping("/{gameId}/promote")
    public ResponseEntity<Void> promotePawn(@PathVariable UUID gameId, @RequestBody PromotionRequest promotionRequest) {
        UUID userId = getAuthenticatedUserId();
        try {
            gameService.handlePromotion(promotionRequest.getPieceType(), gameId, promotionRequest.getPosition(), userId);
            return ResponseEntity.ok().build();
        } catch (InvalidChessException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Chess.demo.modelsandDTO.User) {
            return ((Chess.demo.modelsandDTO.User) authentication.getPrincipal()).getId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }
}
