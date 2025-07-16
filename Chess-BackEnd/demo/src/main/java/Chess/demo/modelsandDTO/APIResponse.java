package Chess.demo.modelsandDTO;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class APIResponse<T> {
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;
    private final String path;
    private final T data;

    public APIResponse(String message, int status, String path , T data) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.data = data;
    }
}
