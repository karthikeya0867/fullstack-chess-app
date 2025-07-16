package Chess.demo.controller;

import Chess.demo.modelsandDTO.APIResponse;
import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.MoveValidationResult;
import Chess.demo.rules.BoardUtils;
import Chess.demo.service.MoveValidationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class GameController {

    private final MoveValidationService moveValidationService;
    private final BoardUtils boardUtils;
    private final GameState gameState;

    public GameController(MoveValidationService moveValidationService,
                          BoardUtils boardUtils,
                          GameState gameState) {
        this.moveValidationService = moveValidationService;
        this.boardUtils = boardUtils;
        this.gameState = gameState;
    }

    @PostMapping("/validate-move")
    public ResponseEntity<APIResponse<MoveValidationResult>> validateMove(
            @Valid @RequestBody Move move,
            HttpServletRequest request) {
        MoveValidationResult result = moveValidationService.validateMove(move);

        String message = result.getMessage();

        APIResponse<MoveValidationResult> response = new APIResponse<>(
                message,
                result.isValid() ? 200 : 400,
                request.getRequestURI(),
                result
        );

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/get-board")
    public String[][] getFrontendBoard() {
        char[][] currentBoard = boardUtils.getBoard();

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

    @GetMapping("/reset-game")
    public String[][] resetGame(){
        gameState.reset();
        return getFrontendBoard();
    }

}
