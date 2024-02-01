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
        return getBoard().getPiece(startPosition).pieceMoves(getBoard(), startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece pieceToMove = getBoard().getPiece(move.getStartPosition());
        Collection<ChessMove> validMoves = pieceToMove.pieceMoves(getBoard(), move.getStartPosition());

        for (ChessMove validMove : validMoves) {
            System.out.println("Test case:");
            System.out.println(move);
            System.out.println(validMove);
            if (move.equals(validMove) && pieceToMove.getTeamColor().equals(getTeamTurn())) {
                getBoard().addPiece(move.getStartPosition(), null);
                getBoard().addPiece(move.getEndPosition(), pieceToMove);
                return;
            }
        }
        throw new InvalidMoveException("Illegal move");
    }

    /**
     *
     * @param teamColor Color of the bishop you wish to find
     * @return ChessPosition of the bishop's location
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
     *
     * @param teamColor Your team's color
     * @return A collection of the opponent's chess pieces
     */
    private Collection<ChessMove> getOpponentPieces(TeamColor teamColor) {
        Collection<ChessMove> pieces = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition newPosition = new ChessPosition(i, j);
                ChessPiece newPiece = getBoard().getPiece(newPosition);

                if (newPiece != null && !newPiece.getTeamColor().equals(teamColor)) {

                    // If opponent's piece is at this location, add all that pieces legal moves
                    pieces.addAll(newPiece.pieceMoves(getBoard(), newPosition));
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
        Collection<ChessMove> opponentMoves = getOpponentPieces(teamColor);

        for (ChessMove opponentMove : opponentMoves) {
            if (opponentMove.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // King is in check
        if (!isInCheck(teamColor)) {
            return false;
        }

        // King cannot move out of check
        ChessPosition kingPosition = getKingPosition(teamColor);
        assert kingPosition != null;
        ChessPiece king = getBoard().getPiece(kingPosition);
        Collection<ChessMove> kingMoves = king.pieceMoves(getBoard(), kingPosition);

        for (ChessMove kingMove : kingMoves) {
            ChessBoard actualBoard = getBoard();
            ChessPiece pieceToMove = getBoard().getPiece(kingMove.getStartPosition());
            getBoard().addPiece(kingMove.getStartPosition(), null);
            getBoard().addPiece(kingMove.getEndPosition(), pieceToMove);

            // If the king can move out of check
            if (!isInCheck(teamColor)) {
                setBoard(actualBoard);
                return false;
            }

            setBoard(actualBoard);
        }

        // Nothing can capture the piece that is checking the king
//        Collection<ChessMove> opponentMoves = getOpponentPieces(teamColor);
//        Collection<ChessPiece> checkingPieces = new HashSet<>();
//        for (ChessMove opponentMove : opponentMoves) {
//            if (opponentMove.getEndPosition().equals(kingPosition)) {
//                checkingPieces.add(getBoard().getPiece(opponentMove.getStartPosition()));
//            }
//        }

        // No piece can block checking pieces
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
        return isInCheck(teamColor) && !isInCheckmate(teamColor);
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
