const ChessBoard = ({ gameState, setGameState, authToken, gameId, userId, setLastMoveResult, showAlert }) => {
  const {
    handleClick,
    handleDragStart,
    handleDragEnd,
    selectedPiece,
    showPromotionModal,
    promotionSquare,
    handlePromotionSelection,
    isDraggable,
  } = useGameLogic({
    gameState,
    setGameState,
    authToken,
    gameId,
    userId,
    setLastMoveResult,
    showAlert,
  });

  // Helper to map piece char to asset name
  const getPieceAssetName = (pieceChar) => {
    if (!pieceChar || pieceChar === ' ') return null;
    const color = utils.getPieceColor(pieceChar) === "White" ? 'w' : 'b';
    const type = utils.getPieceType(pieceChar).toLowerCase();
    return `${color}-${type}`;
  };

  const promotionPieces = ['Q', 'R', 'B', 'N']; // Queen, Rook, Bishop, Knight

  // Get the last move from game history for highlighting
  const lastMove = gameState.moveHistory.length > 0
    ? gameState.moveHistory[gameState.moveHistory.length - 1]
    : null;


  return (
    <div className="relative w-[90vw] max-w-[600px] aspect-square mt-2 ml-[10px] md:ml-4 grid grid-rows-8">
      {showPromotionModal && (
        <div className="absolute inset-0 flex items-center justify-center z-30 bg-black/70">
          <motion.div
            className="flex justify-center items-center flex-col 
              bg-[#1a1a1a] text-[#cccccc] p-6 max-w-[300px] w-full 
              rounded-lg shadow-xl shadow-black
              border border-[#1a1a1a] z-40"
            variants={{
              initial: { opacity: 0, y: -100 },
              animate: { opacity: 1, y: 0 },
            }}
            initial="initial"
            animate="animate"
          >
            <h2 className="text-xl font-bold mb-4">Promote Pawn to:</h2>
            <div className="grid grid-cols-2 gap-4">
              {promotionPieces.map((pieceTypeChar) => (
                <button
                  key={pieceTypeChar}
                  onClick={() => handlePromotionSelection(utils.getPieceType(pieceTypeChar))}
                  className="p-2 bg-[#739552] hover:bg-[#8da35c] rounded flex items-center justify-center"
                >
                  <img src={`./src/assets/pieces-basic-svg/${getPieceAssetName(pieceTypeChar)}.svg`} alt={pieceTypeChar} className="w-12 h-12" />
                </button>
              ))}
            </div>
          </motion.div>
        </div>
      )}

      {constants.chessBoardPositions.map((row, rowIndex) => (
        <div key={rowIndex} className="grid grid-cols-8 w-full h-full">
          {row.map((cell, cellIndex) => {
            const isWhiteSquare = (rowIndex + cellIndex) % 2 === 0;
            const pieceChar = gameState.board[rowIndex][cellIndex];
            const pieceAssetName = getPieceAssetName(pieceChar);
            
            return (
              <Square
                key={cell}
                position={cell}
                pieceChar={pieceChar}
                pieceAssetName={pieceAssetName}
                isWhiteSquare={isWhiteSquare}
                isSelected={selectedPiece === cell}
                onClick={handleClick}
                onDragStart={handleDragStart}
                onDragEnd={handleDragEnd}
                isDraggable={isDraggable(pieceChar)}
                lastMove={lastMove} // Pass lastMove prop
              />
            );
          })}
        </div>
      ))}
    </div>
  );
};

export default ChessBoard;

// Memoized Square
const Square = memo(
  ({
    position,
    pieceChar,
    pieceAssetName,
    isWhiteSquare,
    isSelected,
    onClick,
    onDragStart,
    onDragEnd,
    isDraggable,
    lastMove, // Receive lastMove prop
  }) => {
    const isLastMoveSquare = lastMove && (lastMove.from === position || lastMove.to === position);

    return (
      <div
        data-position={position}
        onClick={onClick}
        className={`relative aspect-square w-full h-full flex items-center justify-center ${
          isWhiteSquare ? "bg-[#ebecd0]" : "bg-[#739552]"
        } ${isLastMoveSquare ? "last-move-highlight" : ""}`} // Apply highlight class
      >
        {isSelected && pieceChar !== ' ' && (
          <div className="absolute inset-0 bg-[#b9ca43] opacity-60 z-10 pointer-events-none" />
        )}
        {pieceChar !== ' ' && pieceAssetName && (
          <motion.img
            src={`./src/assets/pieces-basic-svg/${pieceAssetName}.svg`}
            layoutId={position}
            alt={pieceChar}
            className={`w-full h-full z-20 touch-none select-none ${isDraggable ? 'cursor-grab active:cursor-grabbing' : 'cursor-default'}`}
            drag={isDraggable}
            dragElastic={false}
            dragMomentum={false}
            onDragStart={() => onDragStart(position, pieceChar)}
            onDragEnd={onDragEnd}
            dragSnapToOrigin
          />
        )}
      </div>
    );
  }
);
