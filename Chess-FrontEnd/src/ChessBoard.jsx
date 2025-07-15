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
      [
        "b-pawn",
        "b-pawn",
        "b-pawn",
        "b-pawn",
        "b-pawn",
        "b-pawn",
        "b-pawn",
        "b-pawn",
      ],
      [null, null, null, null, null, null, null, null],
      [null, null, null, null, null, null, null, null],
      [null, null, null, null, null, null, null, null],
      [null, null, null, null, null, null, null, null],
      [
        "w-pawn",
        "w-pawn",
        "w-pawn",
        "w-pawn",
        "w-pawn",
        "w-pawn",
        "w-pawn",
        "w-pawn",
      ],
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

  useEffect(() => {
    fetch(`${API_CONTEXT_PATH}/get-board`)
      .then((response) => response.json())
      .then((data) => setBoardState(data))
      .catch((error) => console.log(error));
  }, []);

  const pieceMapping = (piece) => {
    console.log("piece: ", piece);
    return piece.split("-")[1].toUpperCase();
  };

  const pieceColorMapping = (piece) => {
    console.log("piece: ", piece);
    return piece[0] === "w" ? "White" : "Black";
  };
  class MoveFormat {
    constructor(from, to, pieceColor, pieceType) {
      this.from = from;
      this.to = to;
      this.pieceType = pieceType;
      this.pieceColor = pieceColor;
    }
  }

  const apiValidation = async (move) => {
    try {
      const response = await fetch(`${API_CONTEXT_PATH}/validate-move`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(move),
      });

      const data = await response.json();
      console.log(data);
      console.log(data.message);

      return data;
    } catch (error) {
      console.log(error);
    }
  };

  const [boardState, setBoardState] = useState(defaultBoard);
  const [selectedPiece, setSelectedPiece] = useState(null);

  useImperativeHandle(ref, () => ({
    resetBoard: async () => {
      try {
        const res = await fetch(`${API_CONTEXT_PATH}/reset-game`);
        const data = await res.json();
        setBoardState(data);
      } catch (err) {
        console.log(err);
      }
    },
  }));

  useEffect(() => {
    setSelectedPiece(null);
  }, [boardState]);

  const posToIndex = ([colChar, rowChar]) => {
    const row = 8 - parseInt(rowChar); // 8 - 6 , ....
    const col = colChar.charCodeAt(0) - "a".charCodeAt(0); // 'h' - 'a' , ...
    return [row, col];
  };
  const indexToPos = (targetRow, targetCol) => {
    const rowChar = 8 - targetRow;
    const colChar = String.fromCharCode("a".charCodeAt(0) + targetCol);
    return colChar + rowChar;
  };

  const handleDrop = useCallback(async (e, from) => {
    if (!from || !draggedPieceRef.current) {
      console.warn("Invalid drop : missing from or piece");
      return;
    }
    console.log("from: ", from);
    const boardCoord = boardRef.current.getBoundingClientRect(); //this method gets all coordinates/distances wrt to board from viewport to top , left and width of board ...etc
    const squareWidth = boardCoord.width / 8;

    //clientX and Y return X,Y coords of touchpoint wrt to viewport
    const x = e.clientX - boardCoord.left; //distance from left edge of board to cursor
    const y = e.clientY - boardCoord.top; //distance from top edge of board to cursor

    const targetRow = Math.floor(y / squareWidth);
    const targetCol = Math.floor(x / squareWidth);

    if (targetCol < 0 || targetCol > 7 || targetRow < 0 || targetRow > 7)
      return;
    const [row, col] = posToIndex([from[0], from[1]]);
    const piece = draggedPieceRef.current;
    console.log("fromRow: ", row, "fromCol: ", col);
    console.log(boardState[row][col]);

    const move = new MoveFormat(
      indexToPos(row, col),
      indexToPos(targetRow, targetCol),
      pieceColorMapping(piece),
      pieceMapping(piece)
    );
    const isValid = await apiValidation(move);
    // if (isValid.message === "Success") {
    //   setBoardState((prev) => {
    //     const newBoard = [...prev];
    //     newBoard[row] = [...newBoard[row]];
    //     newBoard[row][col] = null;
    //     newBoard[targetRow] = [...newBoard[targetRow]];
    //     newBoard[targetRow][targetCol] = piece;
    //     return newBoard;
    //   });
    // }
    if (isValid.message === "Success") {
      setBoardState((prev) => {
        const newBoard = [...prev.map((row) => [...row])]; // deep clone

        // Move king
        newBoard[row][col] = null;
        newBoard[targetRow][targetCol] = piece;

        // Detect castling: if king moved two squares horizontally
        if (pieceMapping(piece) === "KING" && Math.abs(targetCol - col) === 2) {
          if (targetCol > col) {
            // King-side castling
            newBoard[row][7] = null;
            newBoard[row][targetCol - 1] = `${piece[0]}-rook`;
          } else {
            // Queen-side castling
            newBoard[row][0] = null;
            newBoard[row][targetCol + 1] = `${piece[0]}-rook`;
          }
        }

        return newBoard;
      });
    } else {
      setAlert({
        message: isValid.message,
      });
    }
    draggingRef.current = null; // reset dragging state after drop
    draggedPieceRef.current = null;
  }, []);

  const handleClick = useCallback(
    async (e) => {
      if (draggingRef.current) return;

      const clickedPos = e.currentTarget.dataset.position; //currentTarget refers to DOM element to which listener is actually attached so even the click on img bubbles up to div
      const [toCol, toRow] = clickedPos;
      if (selectedPiece === clickedPos) {
        setSelectedPiece(null);
        return;
      }
      const [row, col] = posToIndex([toCol, toRow]);
      const targetCol = col;

      if (selectedPiece) {
        const [fromCol, fromRow] = selectedPiece;
        const [frow, fcol] = posToIndex([fromCol, fromRow]);
        const move = new MoveFormat(
          selectedPiece,
          clickedPos,
          pieceColorMapping(boardState[frow][fcol]),
          pieceMapping(boardState[frow][fcol])
        );
        const isValid = await apiValidation(move);

        if (isValid.message === "Success") {
          setBoardState((prev) => {
            const pieceAtFrom = prev[frow][fcol];
            if (!pieceAtFrom) return prev;

            const newBoard = [...prev];
            newBoard[frow] = [...newBoard[frow]];
            newBoard[frow][fcol] = null;
            newBoard[row] = [...newBoard[row]];
            newBoard[row][col] = pieceAtFrom;

            // Detect castling: if king moved two squares horizontally
            if (
              pieceMapping(pieceAtFrom) === "KING" &&
              Math.abs(targetCol - col) === 2
            ) {
              if (targetCol > col) {
                // King-side castling
                newBoard[row][7] = null;
                newBoard[row][targetCol - 1] = `${piece[0]}-rook`;
              } else {
                // Queen-side castling
                newBoard[row][0] = null;
                newBoard[row][targetCol + 1] = `${piece[0]}-rook`;
              }
            }

            return newBoard;
          });
        } else {
          setAlert({
            message: isValid.message,
          });
        }
      } else {
        setSelectedPiece(clickedPos); // if no piece is selected before
      }
    },
    [selectedPiece]
  );

  const handleDragStart = useCallback((from, piece) => {
    console.log("dragging from: ", from);
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
  }) => {
    return (
      <div
        data-position={position}
        onClick={onClick}
        className={`relative aspect-square w-full h-full flex items-center justify-center 
          ${isWhite ? "bg-[#ebecd0]" : "bg-[#739552]"}`}
        draggable={false}
      >
        {isSelected && piece && (
          <div className="absolute inset-0 bg-[#b9ca43] opacity-60 z-10 pointer-events-none"></div>
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
    );
  }
);
