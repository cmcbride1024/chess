package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard gameBoard = new ChessBoard();
    private boolean whiteKingMoved = false, blackKingMoved = false;
    private boolean whiteQueenRookMoved = false, whiteKingRookMoved = false, blackQueenRookMoved = false, blackKingRookMoved = false;
    private boolean canEnPassant = false;
    private ChessPosition enPassantPosition = null;
    private boolean gameIsOver = false;

    public ChessGame() {
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> legalMoves = getBoard().getPiece(startPosition).pieceMoves(getBoard(), startPosition);
        Collection<ChessMove> moves = new HashSet<>();

        for (ChessMove legalMove : legalMoves) {
            if (isValidMove(legalMove, getBoard().getPiece(legalMove.getStartPosition()).getTeamColor())) {
                moves.add(legalMove);
            }
        }

        ChessPiece piece = getBoard().getPiece(startPosition);
        TeamColor teamColor = piece.getTeamColor();

        // Checks if castling moves are valid
        if (piece.getPieceType().equals(ChessPiece.PieceType.KING)) {
            int row = (teamColor == TeamColor.WHITE ? 1 : 8);
            if (startPosition.getRow() == row && canCastleKingside(teamColor)) {
                moves.add(new ChessMove(startPosition, new ChessPosition(row, 7), null));
            }

            if (startPosition.getRow() == row && canCastleQueenside(teamColor)) {
                moves.add(new ChessMove(startPosition, new ChessPosition(row, 3), null));
            }
        }

        // Checks if piece can en passant
        if (piece.getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            int dir = (teamColor == TeamColor.WHITE ? 1 : -1);
            if (canEnPassant && Math.abs(enPassantPosition.getColumn() - startPosition.getColumn()) == 1) {
                moves.add(new ChessMove(startPosition, new ChessPosition(enPassantPosition.getRow() + dir, enPassantPosition.getColumn()), null));
            }
        }

        return moves;
    }

    /**
     * Method that determines whether a move performed on a test board
     * puts a team in check or not
     * @param color teamColor that performs the move
     * @param board A test board which where a move has been performed
     * @return boolean whether the color is in check
     */
    private boolean moveIsInCheck(TeamColor color, ChessBoard board) {
        ChessPosition kingPosition = null;
        Collection<ChessMove> opponentMoves = new HashSet<>();

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition newPosition = new ChessPosition(i, j);
                ChessPiece newPiece = board.getPiece(newPosition);

                if (newPiece != null) {

                    if (newPiece.getTeamColor().equals(color) && newPiece.getPieceType().equals(ChessPiece.PieceType.KING)) {
                        kingPosition = newPosition;
                    }

                    if (newPiece.getTeamColor() != color) {
                        opponentMoves.addAll(newPiece.pieceMoves(board, newPosition));
                    }
                }

            }
        }
        for (ChessMove opponentMove : opponentMoves) {
            if (opponentMove.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    private boolean kingRookMoved(TeamColor teamColor, int rookFile) {
        if (rookFile == 1) {
           switch (teamColor) {
               case WHITE -> {
                   return whiteQueenRookMoved || whiteKingMoved;
               }
               case BLACK -> {
                   return blackQueenRookMoved || blackKingMoved;
               }
           }
       } else if (rookFile == 8) {
           switch (teamColor) {
               case WHITE -> {
                   return whiteKingRookMoved || whiteKingMoved;
               }
               case BLACK -> {
                   return blackKingRookMoved || blackKingMoved;
               }
           }
       }
       return false;
    }

    public boolean canCastleKingside(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        int row = (teamColor == TeamColor.WHITE ? 1 : 8);
        ChessPosition kingPosition = new ChessPosition(row, 5);
        ChessPosition rookPosition = new ChessPosition(row, 8);


        // King and rook have not moved
        if (kingRookMoved(teamColor, 8)) {
            return false;
        }

        // Pieces between king and rook are empty
        for (int col = kingPosition.getColumn() + 1; col < rookPosition.getColumn(); col++) {
            if (getBoard().getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }

        // The king can't move through check
        for (int col = kingPosition.getColumn(); col <= kingPosition.getColumn() + 2; col++) {
            if (movePutsKingInCheck(new ChessPosition(row, col), teamColor)) {
                return false;
            }
        }
        return true;
    }

    public boolean canCastleQueenside(TeamColor teamColor) {
        if (isInCheck(teamColor)) {

            return false;
        }

        int row = (teamColor == TeamColor.WHITE ? 1 : 8);
        ChessPosition kingPosition = new ChessPosition(row, 5);
        ChessPosition rookPosition = new ChessPosition(row, 1);

        // King and rook have not moved
        if (kingRookMoved(teamColor, 1)) {
            return false;
        }

        // Pieces between king and rook are empty
        for (int col = kingPosition.getColumn() - 1; col > rookPosition.getColumn(); col--) {
            if (getBoard().getPiece(new ChessPosition(row, col)) != null) {
                return false;
            }
        }

        // King does not move through or into check
        for (int col = kingPosition.getColumn(); col >= kingPosition.getColumn() - 2; col--) {
            if (movePutsKingInCheck(new ChessPosition(row, col), teamColor)) {
                return false;
            }
        }
        return true;
    }

    private boolean movePutsKingInCheck(ChessPosition position, TeamColor teamColor) {
        ChessBoard testBoard = getBoard().deepCopy();
        testBoard.addPiece(position, new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        return moveIsInCheck(teamColor, testBoard);
    }

    private void castleKingside(TeamColor teamColor) throws InvalidMoveException {
        if (!canCastleKingside(teamColor)) {
            throw new InvalidMoveException("Cannot castle kingside");
        }

        int row = (teamColor == TeamColor.WHITE ? 1 : 8);

        getBoard().addPiece(new ChessPosition(row, 5), null);
        getBoard().addPiece(new ChessPosition(row, 7), new ChessPiece(teamColor, ChessPiece.PieceType.KING));

        getBoard().addPiece(new ChessPosition(row, 8), null);
        getBoard().addPiece(new ChessPosition(row, 6), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
    }

    private void castleQueenside(TeamColor teamColor) throws InvalidMoveException {
        if (!canCastleQueenside(teamColor)) {
            throw new InvalidMoveException("Cannot castle queenside");
        }

        int row = (teamColor == TeamColor.WHITE ? 1 : 8);

        getBoard().addPiece(new ChessPosition(row, 5), null);
        getBoard().addPiece(new ChessPosition(row, 3), new ChessPiece(teamColor, ChessPiece.PieceType.KING));

        getBoard().addPiece(new ChessPosition(row, 1), null);
        getBoard().addPiece(new ChessPosition(row, 4), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
    }

    /**
     * Makes a move in a chess game
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (isInCheckmate(TeamColor.WHITE) || isInCheckmate(TeamColor.BLACK)) {
            throw new InvalidMoveException("Game is over");
        }

        ChessPiece pieceToMove = getBoard().getPiece(move.getStartPosition());

        if (pieceToMove == null) {
            throw new InvalidMoveException("No piece at starting position.");
        }

        TeamColor pieceColor = pieceToMove.getTeamColor();

        if (pieceColor != getTeamTurn()) {
            throw new InvalidMoveException("Move is played out of turn");
        }

        // Throws an error if a given move isn't valid
        if (!isValidMove(move, getTeamTurn())) {
            int row = (pieceColor == TeamColor.WHITE ? 1 : 8);
            ChessMove castleKingside = new ChessMove(new ChessPosition(row, 5), new ChessPosition(row, 7), null);
            ChessMove castleQueenside = new ChessMove(new ChessPosition(row, 5), new ChessPosition(row, 3), null);

            if (canCastleKingside(pieceColor) && move.equals(castleKingside)) {
                castleKingside(pieceColor);
            } else if (canCastleQueenside(pieceColor) && move.equals(castleQueenside)) {
                castleQueenside(pieceColor);
            } else if (canEnPassant) {
                int dir = (pieceColor == TeamColor.WHITE ? 1 : -1);
                ChessMove enPassantMove = new ChessMove(move.getStartPosition(), new ChessPosition(enPassantPosition.getRow() + dir, enPassantPosition.getColumn()), null);

                if (move.equals(enPassantMove)) {
                    getBoard().addPiece(enPassantMove.getStartPosition(), null);
                    getBoard().addPiece(enPassantPosition, null);
                    getBoard().addPiece(enPassantMove.getEndPosition(), new ChessPiece(teamTurn, ChessPiece.PieceType.PAWN));
                } else {
                    throw new InvalidMoveException("Illegal move.");
                }
            } else {
                throw new InvalidMoveException("Illegal move.");
            }
        } else {
            getBoard().addPiece(move.getStartPosition(), null);
            if (move.getPromotionPiece() != null) {
                getBoard().addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
            } else {
                getBoard().addPiece(move.getEndPosition(), pieceToMove);
            }
        }

        // Keeps track of which pieces for castling logic
        if (pieceToMove.getTeamColor().equals(TeamColor.WHITE)) {
            if (pieceToMove.getPieceType().equals(ChessPiece.PieceType.ROOK)) {
                if (!whiteQueenRookMoved && move.getStartPosition().getColumn() == 1) {
                    whiteQueenRookMoved = true;
                } else if (!whiteKingRookMoved && move.getStartPosition().getColumn() == 8) {
                    whiteKingRookMoved = true;
                }
            } else if (pieceToMove.getPieceType().equals(ChessPiece.PieceType.KING)) {
                if (!whiteKingMoved) {
                    whiteKingMoved = true;
                }
            }
        } else {
            if (pieceToMove.getPieceType().equals(ChessPiece.PieceType.ROOK)) {
                if (!blackQueenRookMoved && move.getStartPosition().getColumn() == 1) {
                    blackQueenRookMoved = true;
                } else if (!blackKingRookMoved && move.getStartPosition().getColumn() == 8) {
                    blackKingRookMoved = true;
                }
            } else if (pieceToMove.getPieceType().equals(ChessPiece.PieceType.KING)) {
                if (!blackKingMoved) {
                    blackKingMoved = true;
                }
            }
        }

        // Handles en passant logic
        if (pieceToMove.getPieceType().equals(ChessPiece.PieceType.PAWN)) {
            if (Math.abs(move.getEndPosition().getRow() - move.getStartPosition().getRow()) == 2) {
                canEnPassant = true;
                enPassantPosition = move.getEndPosition();
            } else {
                canEnPassant = false;
                enPassantPosition = null;
            }
        } else {
            canEnPassant = false;
            enPassantPosition = null;
        }

        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     * Returns the position of the king for the specified color.
     * @param teamColor Color of the king you wish to find
     * @return ChessPosition of the king's location
     */
    private ChessPosition getKingPosition(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition newPosition = new ChessPosition(i, j);
                ChessPiece newPiece = getBoard().getPiece(newPosition);

                if (newPiece != null && newPiece.getPieceType().equals(ChessPiece.PieceType.KING) && newPiece.getTeamColor().equals(teamColor)) {
                    return newPosition;
                }

            }
        }
        return null;
    }

    /**
     * Returns a collection of Chess moves for a given team color
     * @param teamColor Team color
     * @return A collection of the possible chess moves
     */
    private Collection<ChessMove> getBoardMoves(TeamColor teamColor) {
        Collection<ChessMove> pieces = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition newPosition = new ChessPosition(i, j);
                ChessPiece newPiece = getBoard().getPiece(newPosition);

                if (newPiece != null && newPiece.getTeamColor().equals(teamColor)) {

                    // If opponent's piece is at this location, add all that pieces legal moves
                    ChessPiece piece = getBoard().getPiece(newPosition);
                    pieces.addAll(piece.pieceMoves(getBoard(), newPosition));
                }

            }
        }

        return pieces;
    }

    /**
     * Determines if the given team is in check
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(teamColor);
        Collection<ChessMove> boardMoves = (teamColor == TeamColor.WHITE ? getBoardMoves(TeamColor.BLACK) : getBoardMoves(TeamColor.WHITE));

        for (ChessMove boardMove : boardMoves) {
            if (boardMove.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether a hypothetical move for a team is legal
     * @param move Takes in a hypothetical move
     * @param teamColor Piece color that is moving
     * @return Boolean whether the move is valid
     */
    private boolean isValidMove(ChessMove move, TeamColor teamColor) {
        ChessBoard testBoard = getBoard().deepCopy();
        ChessPiece pieceToMove = getBoard().getPiece(move.getStartPosition());
        boolean pieceHasMove = pieceToMove.pieceMoves(getBoard(), move.getStartPosition()).contains(move);
        if (!pieceHasMove) {
            return false;
        }
        if (move.getPromotionPiece() != null) {
            testBoard.addPiece(move.getStartPosition(), null);
            testBoard.addPiece(move.getEndPosition(), new ChessPiece(teamColor, move.getPromotionPiece()));
        } else {
            testBoard.addPiece(move.getStartPosition(), null);
            testBoard.addPiece(move.getEndPosition(), pieceToMove);
        }
        return !moveIsInCheck(pieceToMove.getTeamColor(), testBoard);
    }

    /**
     * Determines if a team has no legal moves
     * @param teamColor which team to check for valid moves
     * @return boolean if team has no valid move
     */
    private boolean teamHasNoValidMove(TeamColor teamColor) {
        for (ChessMove possibleMove : getBoardMoves(teamColor)) {
            if (isValidMove(possibleMove, teamColor)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in checkmate, which here is defined as having
     * no valid moves and in check
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate, otherwise false
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // King is in check
        if (!isInCheck(teamColor)) {
            return false;
        }
        return teamHasNoValidMove(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // King is not in check
        if (isInCheck(teamColor)) {
            return false;
        }
        return teamHasNoValidMove(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        whiteKingMoved = blackKingMoved = whiteQueenRookMoved = whiteKingRookMoved = blackQueenRookMoved = blackKingRookMoved = false;
        canEnPassant = false;
        enPassantPosition = null;
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    public void gameIsOver() {
        gameIsOver = true;
    }

    public boolean getGameIsOver() {
        return gameIsOver;
    }
}