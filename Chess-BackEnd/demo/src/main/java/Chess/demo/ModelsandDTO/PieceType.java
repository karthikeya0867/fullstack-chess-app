package Chess.demo.ModelsandDTO;

import lombok.Getter;

@Getter
public enum PieceType {
    PAWN('p'),
    ROOK('r'),
    KNIGHT('n'),
    BISHOP('b'),
    QUEEN('q'),
    KING('k');

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    private final char symbol;
}

