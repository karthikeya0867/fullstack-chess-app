package Chess.demo.Rules;

import Chess.demo.Exceptions.InvalidChessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ValidationFinder {

    private final Map<Character,MoveValidator> validatorMap;
    @Autowired
    public ValidationFinder(Map<String,MoveValidator> validators){
        this.validatorMap = new HashMap<>();
        for(Map.Entry<String,MoveValidator> entry : validators.entrySet()){
            String key = entry.getKey();
            String allPieces = "prnbqk";
            if(key.length() == 1 && allPieces.contains(key)){
                validatorMap.put(key.charAt(0),entry.getValue());
            }
        }
    }

    public MoveValidator getValidatorFor(char piece) {
        char lower = Character.toLowerCase(piece);
        MoveValidator validator = validatorMap.get(lower);
        if (validator == null) throw new InvalidChessException("Unknown piece: " + piece);
        return validator;
    }
}

