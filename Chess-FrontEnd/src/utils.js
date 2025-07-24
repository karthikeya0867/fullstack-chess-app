import { API_CONTEXT_PATH } from "./App";

export const fetchData = async (url, options = {}) => {
  try {
    const result = await fetch(url, options);
    return await result.json();
  } catch (err) {
    console.error(err);
    return { error: "Error Validating Message" };
  }
};

export const posToIndex = ([colChar, rowChar]) => [
  8 - parseInt(rowChar),
  colChar.charCodeAt(0) - "a".charCodeAt(0),
];

export const indexToPos = (row, col) =>
  String.fromCharCode("a".charCodeAt(0) + col) + (8 - row);

export const getPieceType = (piece) => piece?.split("-")[1].toUpperCase();

export const getPieceColor = (piece) =>
  piece?.startsWith("w") ? "White" : "Black";

// Validate move via API
export const validateMove = async (move) => {
  const response = await fetchData(`${API_CONTEXT_PATH}/validate-move`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(move),
  });
  return response;
};

export const updateBoardState = (
  prev,
  fromRow,
  fromCol,
  toRow,
  toCol,
  piece
) => {
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

export const buildMove = (from, to, piece) => ({
  from: from,
  to: to,
  pieceColor: getPieceColor(piece),
  pieceType: getPieceType(piece),
});
