package Chess.demo.rules;

import Chess.demo.modelsandDTO.Move;

public interface MoveValidator {
    boolean isValid(Move move);
}
