package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

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

        if (pieceToMove.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Move is played out of turn");
        }

        if (isValidMove(move, getTeamTurn())) {
            if (move.getPromotionPiece() != null) {
                getBoard().addPiece(move.getStartPosition(), null);
                getBoard().addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
            } else {
                getBoard().addPiece(move.getStartPosition(), null);
                getBoard().addPiece(move.getEndPosition(), pieceToMove);
            }
        } else {
            throw new InvalidMoveException("Illegal move");
        }

        switch (getTeamTurn()) {
            case WHITE -> setTeamTurn(TeamColor.BLACK);
            case BLACK -> setTeamTurn(TeamColor.WHITE);
        }
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
        Collection<ChessMove> boardMoves = switch (teamColor) {
            case WHITE -> getBoardMoves(TeamColor.BLACK);
            case BLACK -> getBoardMoves(TeamColor.WHITE);
        };

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
        ChessBoard actualBoard = getBoard().deepCopy();
        ChessPiece pieceToMove = getBoard().getPiece(move.getStartPosition());
        boolean pieceHasMove = false;
        for (ChessMove possibleMove : pieceToMove.pieceMoves(getBoard(), move.getStartPosition())) {
            if (possibleMove.equals(move)) {
                pieceHasMove = true;
                break;
            }
        }

        if (!pieceHasMove) {
            return pieceHasMove;
        }


        if (move.getPromotionPiece() != null) {
            getBoard().addPiece(move.getStartPosition(), null);
            getBoard().addPiece(move.getEndPosition(), new ChessPiece(teamColor, move.getPromotionPiece()));
        } else {
            getBoard().addPiece(move.getStartPosition(), null);
            getBoard().addPiece(move.getEndPosition(), pieceToMove);
        }

        boolean isValid = !isInCheck(teamColor);
        setBoard(actualBoard);

        return isValid;
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