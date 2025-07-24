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
import * as utils from "./utils";
import { API_CONTEXT_PATH } from "./App";
import Button from "./Button";

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
  const [winner, setWinner] = useState(null);

  // Fetch initial board state from API
  useEffect(() => {
    (async () => {
      const data = await utils.fetchData(`${API_CONTEXT_PATH}/get-board`);
      if (!data.error) setBoardState(data);
      else console.error(data.error);
    })();
  }, []);

  // Reset board exposed via ref
  useImperativeHandle(ref, () => ({
    resetBoard: async () => {
      const data = await utils.fetchData(`${API_CONTEXT_PATH}/reset-game`);
      if (!data.error) setBoardState(data);
      else console.error(data.error);
    },
  }));

  useEffect(() => {
    setSelectedPiece(null);
  }, [boardState]);

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

    const [fromRow, fromCol] = utils.posToIndex(from);
    const piece = draggedPieceRef.current;

    const move = utils.buildMove(
      utils.indexToPos(fromRow, fromCol),
      utils.indexToPos(targetRow, targetCol),
      piece
    );

    if (move.from === move.to) return;

    const validation = await utils.validateMove(move);

    if (validation.message === "Success") {
      console.log(validation);
      setBoardState((prev) =>
        utils.updateBoardState(
          prev,
          fromRow,
          fromCol,
          targetRow,
          targetCol,
          piece
        )
      );
      checkMateValidator(validation.data);
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
      const [toRow, toCol] = utils.posToIndex([toColChar, toRowChar]);

      if (!selectedPiece) {
        setSelectedPiece(clickedPos);
        return;
      }

      const [fromColChar, fromRowChar] = selectedPiece;
      const [fromRow, fromCol] = utils.posToIndex([fromColChar, fromRowChar]);
      const piece = boardState[fromRow][fromCol];

      if (!piece) {
        setSelectedPiece(null);
        return;
      }

      const move = utils.buildMove(selectedPiece, clickedPos, piece);

      if (move.from === move.to) return;

      const validation = await utils.validateMove(move);

      if (validation.message === "Success") {
        console.log(validation);
        setBoardState((prev) =>
          utils.updateBoardState(prev, fromRow, fromCol, toRow, toCol, piece)
        );
        checkMateValidator(validation.data);
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

  const checkMateValidator = (validation) => {
    console.log(validation);
    if (validation.isCheckmate != null) {
      setWinner(validation.winner);
    }
  };
  // --- Render ---

  return (
    <div
      ref={boardRef}
      className="relative w-[90vw] max-w-[600px] aspect-square mt-2 ml-[10px] md:ml-4 grid grid-rows-8"
    >
      {winner && (
        <div className="absolute inset-0  flex items-center justify-center z-30">
          <motion.div
            className="flex justify-center items-center flex-col 
  bg-[#1a1a1a]  text-[#cccccc] p-6 max-w-[500px] w-[55%] 
  h-[60vh] max-h-[700px] rounded-lg shadow-xl shadow-black
  border border-[#1a1a1a] z-40"
            variants={{
              initial: { opacity: 0, y: -100 },
              animate: { opacity: 1, y: 0 },
            }}
            initial="initial"
            animate="animate"
          >
            <h2 className="text-3xl font-bold mb-4">Game Over</h2>
            <p className="text-xl font-semibold">{winner} wins!</p>

            <Button
              isButtonDark={false}
              onClick={() => {
                setWinner(null);
                ref.current.resetBoard();
              }}
            >
              Reset Game
            </Button>
          </motion.div>
        </div>
      )}

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
