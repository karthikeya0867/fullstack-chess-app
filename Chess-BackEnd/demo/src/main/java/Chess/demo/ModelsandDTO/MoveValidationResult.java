package Chess.demo.ModelsandDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveValidationResult {
    private String message;
    private PieceColor winner;
    private PieceColor inCheck;
    private PieceColor isCheckmate;
    private boolean isStalemate;
    private boolean valid;
}
