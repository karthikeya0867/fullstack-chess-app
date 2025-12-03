export const posToIndex = ([colChar, rowChar]) => [
  8 - parseInt(rowChar),
  colChar.charCodeAt(0) - "a".charCodeAt(0),
];

export const indexToPos = (row, col) =>
  String.fromCharCode("a".charCodeAt(0) + col) + (8 - row);

export const getPieceType = (pieceChar) => {
  const char = String(pieceChar).toLowerCase();
  switch (char) {
    case 'p': return 'PAWN';
    case 'r': return 'ROOK';
    case 'n': return 'KNIGHT';
    case 'b': return 'BISHOP';
    case 'q': return 'QUEEN';
    case 'k': return 'KING';
    default: return '';
  }
};

export const getPieceColor = (pieceChar) =>
  (pieceChar === String(pieceChar).toUpperCase()) ? "White" : "Black";

export const getPieceChar = (piece) => { // Converts "w-pawn" to 'P', "b-rook" to 'r'
  if (!piece) return null;
  const [color, type] = piece.split('-');
  let char = type.charAt(0);
  if (color === 'w') char = char.toUpperCase();
  return char;
};


export const updateBoardState = (
  prevBoard,
  fromRow,
  fromCol,
  toRow,
  toCol,
  pieceChar, // Now expects piece character
  enPassantTarget = null // For en passant capture visual update
) => {
  const newBoard = JSON.parse(JSON.stringify(prevBoard)); // Deep copy

  newBoard[toRow][toCol] = pieceChar;
  newBoard[fromRow][fromCol] = ' ';

  // Handle en passant visual removal of captured pawn
  if (
    pieceChar.toLowerCase() === 'p' &&
    Math.abs(fromCol - toCol) === 1 &&
    newBoard[toRow][toCol] !== ' ' && // The pawn moved diagonally
    prevBoard[toRow][toCol] === ' ' // Target square was empty
  ) {
    // Determine where the captured pawn should be (one row behind the 'to' square of the attacking pawn)
    const capturedPawnRow = fromRow; // Same row as the attacking pawn
    const capturedPawnCol = toCol; // Same column as the target square
    newBoard[capturedPawnRow][capturedPawnCol] = ' ';
  }

  // Handle Castling visual movement of rook
  if (pieceChar.toLowerCase() === 'k' && Math.abs(fromCol - toCol) === 2) {
      if (toCol === 6) { // Kingside castling
          newBoard[fromRow][5] = newBoard[fromRow][7]; // Move rook
          newBoard[fromRow][7] = ' '; // Clear old rook position
      } else if (toCol === 2) { // Queenside castling
          newBoard[fromRow][3] = newBoard[fromRow][0]; // Move rook
          newBoard[fromRow][0] = ' '; // Clear old rook position
      }
  }

  return newBoard;
};

export const buildMove = (from, to, pieceChar, pieceColor) => ({
  from: from,
  to: to,
  pieceColor: pieceColor,
  pieceType: getPieceType(pieceChar),
});
