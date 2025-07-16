import {
  useRef,
  useState,
  useCallback,
  memo,
  useMemo,
  useEffect,
  useImperativeHandle,
  forwardRef,
} from "react";
import { motion } from "framer-motion";
import fetchData from "./fetchData";
import { API_CONTEXT_PATH } from "./App";

const ChessBoard = forwardRef(({ setAlert }, ref) => {
  const boardRef = useRef();
  const draggingRef = useRef(null);
  const draggedPieceRef = useRef(null);

  const chessBoardPositions = useMemo(
    () => [
      ["a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8"],
      ["a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7"],
      ["a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6"],
      ["a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5"],
      ["a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4"],
      ["a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3"],
      ["a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2"],
      ["a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"],
    ],
    []
  );

  const defaultBoard = useMemo(
    () => [
      [
        "b-rook",
        "b-knight",
        "b-bishop",
        "b-queen",
        "b-king",
        "b-bishop",
        "b-knight",
        "b-rook",
      ],
      Array(8).fill("b-pawn"),
      Array(8).fill(null),
      Array(8).fill(null),
      Array(8).fill(null),
      Array(8).fill(null),
      Array(8).fill("w-pawn"),
      [
        "w-rook",
        "w-knight",
        "w-bishop",
        "w-queen",
        "w-king",
        "w-bishop",
        "w-knight",
        "w-rook",
      ],
    ],
    []
  );

  const [boardState, setBoardState] = useState(defaultBoard);
  const [selectedPiece, setSelectedPiece] = useState(null);

  // Fetch initial board state from API
  useEffect(() => {
    (async () => {
      const data = await fetchData(`${API_CONTEXT_PATH}/get-board`);
      if (!data.error) setBoardState(data);
      else console.error(data.error);
    })();
  }, []);

  // Reset board exposed via ref
  useImperativeHandle(ref, () => ({
    resetBoard: async () => {
      const data = await fetchData(`${API_CONTEXT_PATH}/reset-game`);
      if (!data.error) setBoardState(data);
      else console.error(data.error);
    },
  }));

  useEffect(() => {
    setSelectedPiece(null);
  }, [boardState]);

  // --- Helpers ---

  const posToIndex = ([colChar, rowChar]) => [
    8 - parseInt(rowChar),
    colChar.charCodeAt(0) - "a".charCodeAt(0),
  ];

  const indexToPos = (row, col) =>
    String.fromCharCode("a".charCodeAt(0) + col) + (8 - row);

  const getPieceType = (piece) => piece?.split("-")[1].toUpperCase();

  const getPieceColor = (piece) => (piece?.startsWith("w") ? "White" : "Black");

  // Validate move via API
  const validateMove = async (move) => {
    const response = await fetchData(`${API_CONTEXT_PATH}/validate-move`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(move),
    });
    return response;
  };

  const updateBoardState = (prev, fromRow, fromCol, toRow, toCol, piece) => {
    const newBoard = prev.slice();

    newBoard[fromRow] = [...prev[fromRow]];
    newBoard[toRow] = [...prev[toRow]];

    newBoard[fromRow][fromCol] = null;
    newBoard[toRow][toCol] = piece;

    if (getPieceType(piece) === "KING" && Math.abs(toCol - fromCol) === 2) {
      if (toCol > fromCol) {
        newBoard[toRow][7] = null;
        newBoard[toRow][toCol - 1] = `${piece[0]}-rook`;
      } else {
        newBoard[toRow][0] = null;
        newBoard[toRow][toCol + 1] = `${piece[0]}-rook`;
      }
    }

    return newBoard;
  };

  // --- Event Handlers ---

  const handleDrop = useCallback(async (e, from) => {
    if (!from || !draggedPieceRef.current) {
      console.warn("Invalid drop: missing from or piece");
      return;
    }

    const boardRect = boardRef.current.getBoundingClientRect();
    const squareWidth = boardRect.width / 8;

    const x = e.clientX - boardRect.left;
    const y = e.clientY - boardRect.top;

    const targetRow = Math.floor(y / squareWidth);
    const targetCol = Math.floor(x / squareWidth);

    if (targetRow < 0 || targetRow > 7 || targetCol < 0 || targetCol > 7)
      return;

    const [fromRow, fromCol] = posToIndex(from);
    const piece = draggedPieceRef.current;

    const move = {
      from: indexToPos(fromRow, fromCol),
      to: indexToPos(targetRow, targetCol),
      pieceColor: getPieceColor(piece),
      pieceType: getPieceType(piece),
    };

    const validation = await validateMove(move);

    if (validation.message === "Success") {
      setBoardState((prev) =>
        updateBoardState(prev, fromRow, fromCol, targetRow, targetCol, piece)
      );
    } else {
      setAlert({ message: validation.message });
    }

    draggingRef.current = null;
    draggedPieceRef.current = null;
  }, []);

  const handleClick = useCallback(
    async (e) => {
      if (draggingRef.current) return;

      const clickedPos = e.currentTarget.dataset.position;
      if (selectedPiece === clickedPos) {
        setSelectedPiece(null);
        return;
      }

      const [toColChar, toRowChar] = clickedPos;
      const [toRow, toCol] = posToIndex([toColChar, toRowChar]);

      if (!selectedPiece) {
        setSelectedPiece(clickedPos);
        return;
      }

      const [fromColChar, fromRowChar] = selectedPiece;
      const [fromRow, fromCol] = posToIndex([fromColChar, fromRowChar]);
      const piece = boardState[fromRow][fromCol];

      if (!piece) {
        setSelectedPiece(null);
        return;
      }

      const move = {
        from: selectedPiece,
        to: clickedPos,
        pieceColor: getPieceColor(piece),
        pieceType: getPieceType(piece),
      };

      const validation = await validateMove(move);

      if (validation.message === "Success") {
        setBoardState((prev) =>
          updateBoardState(prev, fromRow, fromCol, toRow, toCol, piece)
        );
      } else {
        setAlert({ message: validation.message });
      }

      setSelectedPiece(null);
    },
    [selectedPiece]
  );

  const handleDragStart = useCallback((from, piece) => {
    draggingRef.current = from;
    draggedPieceRef.current = piece;
  }, []);

  const handleDragEnd = useCallback(
    (e) => {
      handleDrop(e, draggingRef.current);
      draggingRef.current = null;
    },
    [handleDrop]
  );

  // --- Render ---

  return (
    <div
      ref={boardRef}
      className="relative w-[90vw] max-w-[600px] aspect-square mt-2 ml-[10px] md:ml-4 grid grid-rows-8"
    >
      {chessBoardPositions.map((row, rowIndex) => (
        <div key={rowIndex} className="grid grid-cols-8 w-full h-full">
          {row.map((cell, cellIndex) => {
            const isWhite = (rowIndex + cellIndex) % 2 === 0;
            return (
              <Square
                key={cell}
                position={cell}
                piece={boardState[rowIndex][cellIndex]}
                isWhite={isWhite}
                isSelected={selectedPiece === cell}
                onClick={handleClick}
                onDragStart={handleDragStart}
                onDragEnd={handleDragEnd}
                dragBoundary={boardRef}
              />
            );
          })}
        </div>
      ))}
    </div>
  );
});

export default ChessBoard;

// Memoized Square component for performance
const Square = memo(
  ({
    position,
    piece,
    isWhite,
    isSelected,
    onClick,
    onDragStart,
    onDragEnd,
    dragBoundary,
  }) => (
    <div
      data-position={position}
      onClick={onClick}
      className={`relative aspect-square w-full h-full flex items-center justify-center ${
        isWhite ? "bg-[#ebecd0]" : "bg-[#739552]"
      }`}
      draggable={false}
    >
      {isSelected && piece && (
        <div className="absolute inset-0 bg-[#b9ca43] opacity-60 z-10 pointer-events-none" />
      )}
      {piece && (
        <motion.img
          src={`./src/assets/pieces-basic-svg/${piece}.svg`}
          layoutId={position}
          alt={piece}
          className="cursor-grab active:cursor-grabbing w-full h-full z-20 touch-none select-none"
          drag
          dragConstraints={dragBoundary}
          dragElastic={false}
          dragMomentum={false}
          onDragStart={() => onDragStart(position, piece)}
          onDragEnd={onDragEnd}
          dragSnapToOrigin
        />
      )}
    </div>
  )
);
