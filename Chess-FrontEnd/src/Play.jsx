import { useSearchParams, useNavigate } from "react-router-dom";
import Navbar from "./NavBar.jsx";
import ChessBoard from "./ChessBoard.jsx";
import Button from "./Button.jsx";
import { Alert } from "@mui/material";
import { useState, useEffect, useRef } from "react";
import { AnimatePresence, hasWarned, motion } from "framer-motion";

const Play = () => {
  const resetRef = useRef(null);
  const resetGame = () => {
    if (resetRef.current && typeof resetRef.current.resetBoard === "function") {
      resetRef.current.resetBoard();
    }
  };
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [selectedMode, setSelectedMode] = useState(
    searchParams.get("mode") ?? "computer"
  );

  const [alert, setAlert] = useState({
    message: "",
    severity: "warning",
    visible: false,
  });

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
                {alert.message.message}
              </Alert>
            </motion.div>
          )}
        </AnimatePresence>

        <div className="flex flex-col lg:flex-row gap-6 px-6 py-6 w-full justify-center lg:items-center flex-grow">
          <div className="w-full max-w-[600px]">
            <ChessBoard setAlert={showAlert} ref={resetRef} />
          </div>

          <div className="w-full max-w-[350px] bg-[#2f2e2c] rounded-2xl shadow-lg flex flex-col items-center p-6  mx-6 space-y-6">
            <div className="w-full flex flex-col items-center space-y-2">
              <p className="text-white font-bold">Select Modes</p>
              <Button
                isButtonDark={false}
                className="!w-full !bg-[#1e1e1e] !text-[#ffd700]"
                onClick={resetGame}
              >
                + New Game
              </Button>
            </div>

            <div className="flex flex-col w-full space-y-4 items-center p-5">
              <p className="text-white font-bold">Play With</p>
              {["Vs Computer", "Vs Local", "Vs Random"].map((ele, index) => {
                return (
                  <Button
                    isButtonDark={false}
                    key={index}
                    className="!w-full !bg-[#1e1e1e] !text-[#ffd700]"
                    onClick={() => handleModeChange(ele)}
                  >
                    {ele}
                  </Button>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Play;
