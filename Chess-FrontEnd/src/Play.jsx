import { useSearchParams, useNavigate } from "react-router-dom";
import Navbar from "./NavBar.jsx";
import ChessBoard from "./ChessBoard.jsx";
import Button from "./Button.jsx";
import { Alert } from "@mui/material";
import { useState, useEffect, useRef } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { useAuth } from "./AuthContext.jsx"; // Import useAuth
import axios from "axios"; // Import axios
import useWebSocket from "./hooks/useWebSocket.js"; // Import useWebSocket

const Play = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { authToken, userId } = useAuth(); // Get auth token and userId

  const gameId = searchParams.get("gameId");
  const [gameState, setGameState] = useState(null);
  const [loadingGame, setLoadingGame] = useState(true);
  const [gameError, setGameError] = useState(null);
  const [lastMoveResult, setLastMoveResult] = useState(null);

  const API_BASE_URL = "http://localhost:8080/api/games";

  // ... (rest of the component)

  const showAlert = (message) => {
    setAlert({ message, severity: "warning", visible: true });
  };
  const modeMap = {
    "Vs Computer": "computer",
    "Vs Local": "local",
    "Vs Random": "online",
  };

  const handleModeChange = (modeLabel) => {
    const mode = modeMap[modeLabel];
    if (mode) {
      navigate(`/play?mode=${mode}`);
      setSelectedMode(mode);
    }
  };

  useEffect(() => {
    if (alert.visible) {
      const timeout = setTimeout(() => {
        setAlert((prev) => ({ ...prev, visible: false }));
      }, 3000);

      return () => clearTimeout(timeout);
    }
  }, [alert.visible]);

  return (
    <>
      <div className="bg-[#1a1919] w-screen h-screen flex items-center">
        <Navbar />

        <AnimatePresence>
          {alert.visible && (
            <motion.div
              className="fixed bottom-20 right-4 left-4 mx-auto z-10 w-[90vw] max-w-[600px] sm:left-auto sm:right-4 sm:mx-0 sm:bottom-4"
              variants={{
                initial: { opacity: 0, y: 50 },
                animate: { opacity: 1, y: 0 },
                exit: { opacity: 0, y: 100 },
              }}
              initial="initial"
              animate="animate"
              exit="exit"
              transition={{ duration: 0.5, ease: "easeInOut" }}
            >
              <Alert
                severity={alert.severity}
                onClose={() =>
                  setAlert((prev) => ({ ...prev, visible: false }))
                }
              >
                {alert.message}
              </Alert>
            </motion.div>
          )}
        </AnimatePresence>

        {loadingGame ? (
          <div className="text-white text-xl w-full text-center">Loading game...</div>
        ) : gameError ? (
          <div className="text-red-500 text-xl w-full text-center">{gameError}</div>
        ) : !gameState ? (
          <div className="text-white text-xl w-full text-center">No game state available.</div>
        ) : (
          <div className="flex flex-col lg:flex-row gap-6 px-6 py-6 w-full justify-center lg:items-center flex-grow">
            <div className="w-full max-w-[600px]">
              <ChessBoard 
                gameState={gameState}
                setGameState={setGameState}
                authToken={authToken}
                gameId={gameId}
                userId={userId}
                setLastMoveResult={setLastMoveResult}
                showAlert={showAlert} // Pass showAlert to ChessBoard
              />
            </div>

            <div className="w-full max-w-[350px] bg-[#2f2e2c] rounded-2xl shadow-lg flex flex-col items-center p-6  mx-6 space-y-6">
              <div className="w-full flex flex-col items-center space-y-2">
                <p className="text-white font-bold">Game ID: {gameId.substring(0, 8)}...</p>
                <p className="text-white font-bold">
                  Current Turn: {gameState.whiteTurn ? "White" : "Black"}
                </p>
                {lastMoveResult && lastMoveResult.checkmate && (
                  <p className="text-red-500 font-bold">CHECKMATE! {lastMoveResult.winner} wins!</p>
                )}
                {lastMoveResult && lastMoveResult.stalemate && (
                  <p className="text-yellow-500 font-bold">STALEMATE! It's a draw!</p>
                )}
                {lastMoveResult && lastMoveResult.draw && !lastMoveResult.stalemate && (
                  <p className="text-yellow-500 font-bold">DRAW! ({lastMoveResult.message})</p>
                )}
                
                <Button
                  isButtonDark={false}
                  className="!w-full !bg-[#1e1e1e] !text-[#ffd700]"
                  onClick={handleResetGame}
                >
                  Reset Game
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default Play;
