package Chess.demo.Rules;

import Chess.demo.ModelsandDTO.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("q")
public class QueenValidator implements MoveValidator {

    private final RookValidator rookValidator;
    private final BishopValidator bishopValidator;

    @Autowired
    public QueenValidator(RookValidator rookValidator, BishopValidator bishopValidator) {
        this.rookValidator = rookValidator;
        this.bishopValidator = bishopValidator;
    }

    @Override
    public boolean isValid(Move move) {
        return rookValidator.isValid(move) || bishopValidator.isValid(move);
    }
}

