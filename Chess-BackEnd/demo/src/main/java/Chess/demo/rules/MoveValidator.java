package Chess.demo.rules;

import Chess.demo.modelsandDTO.GameState;
import Chess.demo.modelsandDTO.Move;
import Chess.demo.modelsandDTO.PieceColor;

import java.util.ArrayList;

public interface MoveValidator {
    boolean isValid(Move move, GameState gameState);
}
