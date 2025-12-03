package Chess.demo;

import Chess.demo.modelsandDTO.GameState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameStateRepository extends JpaRepository<GameState, UUID> {
}
