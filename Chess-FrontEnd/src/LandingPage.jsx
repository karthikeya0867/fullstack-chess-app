import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import NavBar from "./NavBar.jsx";
import { AnimatePresence, motion } from "framer-motion";
import { SportsEsports } from "@mui/icons-material";
import Button from "./Button.jsx";
import { useAuth } from "./AuthContext.jsx"; // Import useAuth
import axios from "axios"; // Import axios

const words = ["AnyTime", "AnyWhere", "Instantly", "Online"];

const modes = [
  {
    navigate: "./play?mode=local",
    title: "Pass 'n Play",
    desc: "Share the fun on a single device.",
    buttonText: "Vs Local Friend",
    reverse: false,
    imgUrl: "./src/assets/blackrookland.png",
  },
  {
    navigate: "./play?mode=online",
    title: "Play Online",
    desc: "Challenge players around the world.",
    buttonText: "Vs Random",
    reverse: true,
    imgUrl: "./src/assets/whiteknightland.png",
  },
  {
    navigate: "./play?mode=computer",
    title: "Play Against Computer",
    desc: "Test your skills against computer AI.",
    buttonText: "Vs Computer",
    reverse: false,
    imgUrl: "./src/assets/blackbishopland.png",
  },
];

const flipVariants = {
  initial: {
    opacity: 0,
    y: -20,
  },
  animate: {
    opacity: 1,
    y: 0,
  },
  exit: {
    opacity: 0,
    y: 20,
  },
};

const cardVariants = {
  hidden: {
    opacity: 0,
    y: 60,
  },
  visible: {
    opacity: 1,
    y: 0,
  },
};

const LandingPage = () => {
  const [wordIndex, setWordIndex] = useState(0);
  const navigate = useNavigate();
  const { authToken, userId } = useAuth(); // Get auth token and userId
  const [openGames, setOpenGames] = useState([]);
  const [loadingGames, setLoadingGames] = useState(true);
  const [gameError, setGameError] = useState(null);

  const API_BASE_URL = "http://localhost:8080/api/games";

  useEffect(() => {
    const interval = setInterval(() => {
      setWordIndex((prev) => (prev + 1) % words.length);
    }, 2000);
    return () => clearInterval(interval);
  }, []);

  // WebSocket for real-time open games updates
  const onOpenGamesReceived = (games) => {
    setOpenGames(games);
    setLoadingGames(false); // Once connected, stop initial loading state
  };
  const { isConnected } = useWebSocket(
    "/topic/games/open",
    onOpenGamesReceived
  );

  // Initial fetch (if websocket not connected or for first load) - can be removed if websocket is always reliable
  // For robustness, we might keep an initial REST fetch and then rely on WS updates.
  // For now, let's assume WS will handle updates fully.
  // The loadingGames state will primarily be managed by WS connection status and first message.
  useEffect(() => {
    if (!isConnected && authToken) {
      // If not connected and authenticated, try initial REST fetch
      setLoadingGames(true);
      const fetchInitialOpenGames = async () => {
        try {
          const response = await axios.get(`${API_BASE_URL}/open`, {
            headers: { Authorization: `Bearer ${authToken}` },
          });
          setOpenGames(response.data);
        } catch (err) {
          console.error("Failed to fetch initial open games via REST:", err);
          setGameError("Failed to load initial open games.");
        } finally {
          setLoadingGames(false);
        }
      };
      fetchInitialOpenGames();
    } else if (!authToken) {
      setLoadingGames(false);
      setOpenGames([]);
    }
  }, [authToken, isConnected]); // Depend on authToken and isConnected

  const handleCreateGame = async () => {
    if (!authToken) {
      setGameError("You must be logged in to create a game.");
      return;
    }
    setLoadingGames(true);
    setGameError(null);
    try {
      const response = await axios.post(
        `${API_BASE_URL}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
          },
        }
      );
      navigate(`/play?gameId=${response.data.gameId}`);
    } catch (err) {
      console.error("Failed to create game:", err);
      setGameError("Failed to create game.");
    } finally {
      setLoadingGames(false);
    }
  };

  const handleJoinGame = async (gameId) => {
    if (!authToken) {
      setGameError("You must be logged in to join a game.");
      return;
    }
    setLoadingGames(true);
    setGameError(null);
    try {
      await axios.post(
        `${API_BASE_URL}/${gameId}/join`,
        {},
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
          },
        }
      );
      navigate(`/play?gameId=${gameId}`);
    } catch (err) {
      console.error("Failed to join game:", err);
      setGameError(err.response?.data?.message || "Failed to join game.");
    } finally {
      setLoadingGames(false);
    }
  };

  return (
    <>
      <NavBar />
      <div className="flex flex-col items-center">
        <div
          className="relative shadow-2xl shadow-black/70 w-full h-[90vh] bg-cover [background-position:center_33%]"
          style={{ backgroundImage: "url('./src/assets/logo1.png') " }}
        >
          <div className="absolute left-[5%] bottom-[10%] z-10 flex flex-col text-white max-w-[90vw]">
            <div className="font-extrabold flex flex-col gap-1">
              <span className="text-white text-5xl sm:text-8xl leading-none drop-shadow-md">
                Play
              </span>
              <div className="relative h-20 sm:h-28 flex items-start overflow-visible">
                <AnimatePresence mode="wait">
                  <motion.span
                    key={words[wordIndex]}
                    variants={flipVariants}
                    initial="initial"
                    animate="animate"
                    exit="exit"
                    transition={{ duration: 0.6, ease: "easeInOut" }}
                    className="font-bold text-5xl sm:text-8xl leading-none bg-gradient-to-r from-fuchsia-500 to-indigo-700 bg-clip-text text-transparent drop-shadow-md"
                  >
                    {words[wordIndex]}
                  </motion.span>
                </AnimatePresence>
              </div>
            </div>

            <div className="drop-shadow-lg max-w-[600px]">
              <h1 className="text-2xl font-bold mb-2">Play Chess Your Way</h1>
              <p className="text-lg font-normal leading-snug opacity-90">
                Challenge friends, master the AI, or compete globally &nbsp; —
                your game, your rules.
              </p>
            </div>
          </div>
        </div>
      </div>
      <div className="w-full max-w-[1200px] mx-auto py-20 px-6 flex flex-col gap-20 bg-white">
        {modes.map((mode, i) => (
          <motion.div
            key={i}
            className={`flex items-center justify-between gap-10 flex-wrap ${
              mode.reverse ? "flex-row-reverse" : ""
            }`}
            variants={cardVariants}
            initial="hidden"
            whileInView="visible"
            transition={{ duration: 0.6, delay: i * 0.2, ease: "easeInOut" }}
          >
            <div
              className="aspect-[3/4] flex-1 min-w-[300px]  border-2 border-dashed border-gray-200 rounded-xl bg-cover bg-center bg-no-repeat"
              role="img"
              aria-label="Empty placeholder"
              style={{ backgroundImage: `url('${mode.imgUrl}')` }}
            />
            <div className="flex-1 min-w-[300px] text-black">
              <SportsEsports className="!text-[48px] mb-2" />
              <h2 className="text-2xl font-semibold mb-2">{mode.title}</h2>
              <p className="text-base text-gray-800">{mode.desc}</p>
              {mode.navigate === "./play?mode=online" ? (
                // Only show "Play Online" button if authenticated
                authToken ? (
                  <div className="flex flex-col gap-4 mt-4">
                    <Button onClick={handleCreateGame} disabled={loadingGames}>
                      {loadingGames
                        ? "Creating Game..."
                        : "Create New Online Game"}
                    </Button>
                    {gameError && <p className="text-red-500">{gameError}</p>}
                    <h3 className="text-xl font-semibold mt-4">Open Games:</h3>
                    {loadingGames ? (
                      <p>Loading open games...</p>
                    ) : openGames.length === 0 ? (
                      <p>No open games. Create one!</p>
                    ) : (
                      <ul className="list-disc pl-5">
                        {openGames.map((game) => (
                          <li
                            key={game.gameId}
                            className="flex items-center justify-between mb-2"
                          >
                            <span>
                              Game ID: {game.gameId.substring(0, 8)}...
                            </span>
                            <Button
                              onClick={() => handleJoinGame(game.gameId)}
                              disabled={loadingGames}
                            >
                              Join Game
                            </Button>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                ) : (
                  <p className="text-red-500">Login to play online.</p>
                )
              ) : (
                <Button
                  isButtonDark={true}
                  onClick={() => navigate(mode.navigate)}
                >
                  {mode.buttonText}
                </Button>
              )}
            </div>
          </motion.div>
        ))}

        <motion.div
          variants={cardVariants}
          initial="hidden"
          whileInView="visible"
          transition={{ duration: 0.6, delay: 0.2, ease: "easeInOut" }}
        >
          {!authToken && (
            <Link to="/signup">
              <Button isButtonDark={true}>Signup Today</Button>
            </Link>
          )}
        </motion.div>
      </div>
    </>
  );
};

export default LandingPage;
