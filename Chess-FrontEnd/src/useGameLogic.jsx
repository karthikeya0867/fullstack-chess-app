import { useCallback, useRef, useState } from "react";
import * as utils from "./utils"; // Keep utils for position conversions
import { games as apiGames } from "./api"; // Import games API

const useGameLogic = ({
  gameState,
  setGameState,
  authToken,
  gameId,
  userId,
  setLastMoveResult,
  showAlert,
}) => {
  const [selectedPiece, setSelectedPiece] = useState(null);
  const [showPromotionModal, setShowPromotionModal] = useState(false);
  const [promotionSquare, setPromotionSquare] = useState(null);
  const [lastMoveMade, setLastMoveMade] = useState(null); // To store the move that caused promotion

  const handleMove = useCallback(
    async (fromPos, toPos) => {
      // Logic for handling a move (either click-based or drag-and-drop)
      const [fromRow, fromCol] = utils.posToIndex(fromPos);
      const [toRow, toCol] = utils.posToIndex(toPos);

      const pieceChar = gameState.board[fromRow][fromCol];

      if (!pieceChar || pieceChar === ' ') {
        setSelectedPiece(null);
        return;
      }

      const move = utils.buildMove(
        fromPos,
        toPos,
        pieceChar,
        utils.getPieceColor(pieceChar)
      );

      if (move.from === move.to) {
        setSelectedPiece(null);
        return;
      }

      try {
        const response = await apiGames.makeMove(gameId, move);
        setLastMoveResult(response.data.data); // Assuming data field holds MoveValidationResult
        if (response.data.data.message === "Pawn promotion needed") {
          setPromotionSquare(toPos);
          setLastMoveMade(move); // Store move for promotion context
          setShowPromotionModal(true);
        } else if (!response.data.isValid) {
          showAlert(response.data.message);
        }
      } catch (err) {
        console.error("Move API error:", err);
        showAlert(err.response?.data?.message || "Failed to make move.");
      } finally {
        setSelectedPiece(null);
      }
    },
    [gameState, gameId, showAlert, setLastMoveResult, setSelectedPiece]
  );

  const handleClick = useCallback(
    (e) => {
      const clickedPos = e.currentTarget.dataset.position;
      const [clickedRow, clickedCol] = utils.posToIndex(clickedPos);
      const clickedPieceChar = gameState.board[clickedRow][clickedCol];
      const clickedPieceColor = utils.getPieceColor(clickedPieceChar);

      if (selectedPiece === clickedPos) {
        setSelectedPiece(null);
        return;
      }

      if (!selectedPiece) {
        // Select a piece if it's the current player's piece
        if (
          (gameState.whiteTurn && clickedPieceColor === "White" && userId === gameState.whitePlayerId) ||
          (!gameState.whiteTurn && clickedPieceColor === "Black" && userId === gameState.blackPlayerId)
        ) {
          if (clickedPieceChar && clickedPieceChar !== ' ') {
            setSelectedPiece(clickedPos);
          }
        }
        return;
      }

      // If a piece is already selected, attempt to move it
      handleMove(selectedPiece, clickedPos);
    },
    [selectedPiece, gameState, userId, handleMove, setSelectedPiece]
  );

  const handleDragStart = useCallback((fromPos, pieceChar) => {
    // Only allow dragging if it's the current player's piece and turn
    const pieceColor = utils.getPieceColor(pieceChar);
    const isPlayersPiece =
      (gameState.whiteTurn && pieceColor === "White" && userId === gameState.whitePlayerId) ||
      (!gameState.whiteTurn && pieceColor === "Black" && userId === gameState.blackPlayerId);

    if (isPlayersPiece) {
      draggingRef.current = fromPos;
      draggedPieceRef.current = pieceChar;
      setSelectedPiece(fromPos); // Visually select the dragged piece
    } else {
      // Prevent drag if not allowed
      draggingRef.current = null;
      draggedPieceRef.current = null;
    }
  }, [gameState, userId, setSelectedPiece]);

  const handleDragEnd = useCallback(
    (e, from) => {
      if (!draggingRef.current) return; // Drag was not initiated by allowed piece

      const boardRect = e.target.closest('.grid').getBoundingClientRect(); // Get board boundary
      const squareSize = boardRect.width / 8; // Assuming 8x8 board

      const x = e.clientX - boardRect.left;
      const y = e.clientY - boardRect.top;

      const toRow = Math.floor(y / squareSize);
      const toCol = Math.floor(x / squareSize);

      if (toRow < 0 || toRow > 7 || toCol < 0 || toCol > 7) {
        setSelectedPiece(null); // Deselect if dropped out of bounds
        return;
      }

      const toPos = utils.indexToPos(toRow, toCol);
      handleMove(draggingRef.current, toPos);

      draggingRef.current = null;
      draggedPieceRef.current = null;
    },
    [handleMove, setSelectedPiece]
  );

  const handlePromotionSelection = useCallback(async (pieceType) => {
    if (!promotionSquare || !lastMoveMade) return;

    try {
      const response = await apiGames.promotePawn(
        gameId,
        pieceType,
        promotionSquare
      );
      setLastMoveResult(response.data.data);
      if (!response.data.isValid) {
          showAlert(response.data.message);
      }
    } catch (err) {
      console.error("Promotion API error:", err);
      showAlert(err.response?.data?.message || "Failed to promote pawn.");
    } finally {
      setShowPromotionModal(false);
      setPromotionSquare(null);
      setLastMoveMade(null);
    }
  }, [gameId, promotionSquare, lastMoveMade, showAlert, setLastMoveResult]);

  const isDraggable = useCallback((pieceChar) => {
    if (!pieceChar || pieceChar === ' ') return false;
    const pieceColor = utils.getPieceColor(pieceChar);
    return (
      (gameState.isWhiteTurn && pieceColor === "White" && userId === gameState.whitePlayerId) ||
      (!gameState.isWhiteTurn && pieceColor === "Black" && userId === gameState.blackPlayerId)
    );
  }, [gameState, userId]);

  return {
    handleClick,
    handleDragStart,
    handleDragEnd,
    selectedPiece,
    showPromotionModal,
    setShowPromotionModal,
    promotionSquare,
    handlePromotionSelection,
    isDraggable
  };
};

export default useGameLogic;
