package Chess.demo.rules;

import Chess.demo.exceptions.InvalidChessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ValidationFinder {

    private final Map<Character,MoveValidator> validatorMap;
    @Autowired
    public ValidationFinder(Map<String,MoveValidator> validators){
        this.validatorMap = validators.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().charAt(0),
                        Map.Entry::getValue
                ));

    }

    public MoveValidator getValidatorFor(char piece) {
        piece = Character.toLowerCase(piece);
        MoveValidator validator = validatorMap.get(piece);
        if (validator == null) throw new InvalidChessException("Unknown piece: " + piece);
        return validator;
    }
}

