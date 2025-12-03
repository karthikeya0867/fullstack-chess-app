package Chess.demo.rules;

import Chess.demo.exceptions.InvalidChessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ValidationFinder {

    private final Map<Character,MoveValidator> validatorMap;

    public ValidationFinder(){
        this.validatorMap = new HashMap<>();
        validatorMap.put('p', new PawnValidator());
        validatorMap.put('r', new RookValidator());
        validatorMap.put('n', new KnightValidator());
        validatorMap.put('b', new BishopValidator());
        validatorMap.put('q', new QueenValidator());
        validatorMap.put('k', new KingValidator());
    }

    public MoveValidator getValidatorFor(char piece) {
        piece = Character.toLowerCase(piece);
        MoveValidator validator = validatorMap.get(piece);
        if (validator == null) throw new InvalidChessException("Unknown piece: " + piece);
        return validator;
    }
}
