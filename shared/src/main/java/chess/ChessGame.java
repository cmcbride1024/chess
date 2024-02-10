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
     *
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
     *
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

        return moves;
    }

    private boolean moveIsInCheck(ChessMove move, ChessBoard board) {
        ChessPosition kingPosition = null;
        TeamColor color = board.getPiece(move.getEndPosition()).getTeamColor();
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

    /**
     * Makes a move in a chess game
     *
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

        if (pieceToMove.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Move is played out of turn");
        }

        if (!isValidMove(move, getTeamTurn())) {
            throw new InvalidMoveException("Illegal move.");
        }

        // Simulate to see if a given move is legal
        getBoard().addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() != null) {
            getBoard().addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
        } else {
            getBoard().addPiece(move.getEndPosition(), pieceToMove);
        }

        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    /**
     *
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
     *
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
     *
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
     *
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

        return !moveIsInCheck(move, testBoard);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        // King is in check
        if (!isInCheck(teamColor)) {
            return false;
        }

        // King cannot move out of check
        ChessPosition kingPosition = getKingPosition(teamColor);
        assert kingPosition != null;
        Collection<ChessMove> kingMoves = getBoard().getPiece(kingPosition).pieceMoves(getBoard(), kingPosition);
        for (ChessMove kingMove : kingMoves) {
            if (isValidMove(kingMove, teamColor)) {
                return false;
            }
        }

        // Other pieces cannot block or capture the checking piece(s)
        Collection<ChessMove> ourMoves = getBoardMoves(teamColor);
        for (ChessMove move : ourMoves) {
            if (isValidMove(move, teamColor)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // King is in check
        if (isInCheck(teamColor)) {
            return false;
        }

        // King cannot move out of check
        ChessPosition kingPosition = getKingPosition(teamColor);
        assert kingPosition != null;
        Collection<ChessMove> kingMoves = getBoard().getPiece(kingPosition).pieceMoves(getBoard(), kingPosition);
        for (ChessMove kingMove : kingMoves) {
            if (isValidMove(kingMove, teamColor)) {
                return false;
            }
        }

        // Other pieces cannot block or capture the checking piece(s)
        Collection<ChessMove> ourMoves = getBoardMoves(teamColor);
        for (ChessMove move : ourMoves) {
            if (isValidMove(move, teamColor)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}