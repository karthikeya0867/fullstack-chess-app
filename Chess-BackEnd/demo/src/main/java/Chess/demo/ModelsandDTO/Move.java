package Chess.demo.ModelsandDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.validation.annotation.Validated;


@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class Move {

    @NotBlank
    @Size(min = 2, max = 2, message = "From must be exactly 2 characters")
    @Pattern(regexp = "^[A-Ha-h][1-8]$" , message = "From should be an alphabet[a-h] followed by a digit[1-8]")
    private String from;

    @NotBlank
    @Size(min = 2, max = 2, message = "To must be exactly 2 characters")
    @Pattern(regexp = "^[A-Ha-h][1-8]$" , message = "TO should be an alphabet[a-h] followed by a digit[1-8]")
    private String to;

    @NotNull
    private PieceType pieceType;
    @NotNull
    private PieceColor pieceColor;

    public record MoveHistory(String from, String to) {}

}
