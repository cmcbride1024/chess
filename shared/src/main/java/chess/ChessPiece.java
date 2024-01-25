package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType piece = getPieceType();
        return switch(piece) {
            case KING -> Moves.kingMoves(board, myPosition);
            case QUEEN -> Moves.queenMoves(board, myPosition);
            case BISHOP -> Moves.bishopMoves(board, myPosition);
            case KNIGHT -> Moves.knightMoves(board, myPosition);
            case ROOK -> Moves.rookMoves(board, myPosition);
            case PAWN -> Moves.pawnMoves(board, myPosition);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessPiece that = (ChessPiece) o;

        if (pieceColor != that.pieceColor) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = pieceColor != null ? pieceColor.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}

class Moves {
    private static boolean inBounds(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    static Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        int[] up = {1, -1, -1, 1};
        int[] right = {1, 1, -1, -1};

        for (int i = 0; i < up.length; i++) {
            ChessPosition newPosition = new ChessPosition(row + up[i], col + right[i]);

            while (inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                ChessMove newMove = new ChessMove(position, newPosition, null);

                if (newPiece != null) {
                    if (piece.getTeamColor() != newPiece.getTeamColor()) {
                        moveList.add(newMove);
                    }
                    break;
                }

                moveList.add(newMove);
                newPosition = new ChessPosition(newPosition.getRow() + up[i], newPosition.getColumn() + right[i]);
            }
        }

        return moveList;
    }

    static Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        int[] up = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] right = {0, 1, 1, 1, 0, -1, -1, -1};

        for (int i = 0; i < up.length; i++) {
            ChessPosition newPosition = new ChessPosition(row + up[i], col + right[i]);

            if (inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                ChessMove newMove = new ChessMove(position, newPosition, null);

                if (newPiece != null) {
                    if (piece.getTeamColor() != newPiece.getTeamColor()) {
                        moveList.add(newMove);
                    }
                } else {
                    moveList.add(newMove);
                }
            }
        }

        return moveList;
    }

    static Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        int[] up = {2, 1, -1, -2, -2, -1, 1, 2};
        int[] right = {1, 2, 2, 1, -1, -2, -2, -1};

        for (int i = 0; i < up.length; i++) {
            ChessPosition newPosition = new ChessPosition(row + up[i], col + right[i]);

            if (inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                ChessMove newMove = new ChessMove(position, newPosition, null);

                if (newPiece != null) {
                    if (piece.getTeamColor() != newPiece.getTeamColor()) {
                        moveList.add(newMove);
                    }
                } else {
                    moveList.add(newMove);
                }
            }
        }

        return moveList;
    }

    static Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        boolean firstMove = ((piece.getTeamColor() == ChessGame.TeamColor.WHITE) && (row == 2) || (piece.getTeamColor() == ChessGame.TeamColor.BLACK) && (row == 7));
        boolean canPromote = ((piece.getTeamColor() == ChessGame.TeamColor.WHITE) && (row == 7) || (piece.getTeamColor() == ChessGame.TeamColor.BLACK) && (row == 2));

        // Forward moves
        int moveDirection = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
        ChessPosition newPosition = new ChessPosition(row + moveDirection, col);
        ChessPiece newPiece = board.getPiece(newPosition);
        if (newPiece == null) {
            if (canPromote) {
                ChessPiece.PieceType[] promotionPieces = {ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK};
                for (ChessPiece.PieceType promote : promotionPieces) {
                    moveList.add(new ChessMove(position, newPosition, promote));
                }
            } else {
                moveList.add(new ChessMove(position, newPosition, null));
                if (firstMove) {
                    ChessPosition extraPosition = new ChessPosition(row + (2 * moveDirection), col);
                    ChessPiece extraPiece = board.getPiece(extraPosition);
                    if (extraPiece == null) {
                        moveList.add(new ChessMove(position, extraPosition, null));
                    }
                }
            }
        }

        // Diagonal moves
        int[] up = {1, 1};
        int[] right = {1, -1};
        for (int i = 0; i < up.length; i++) {
            ChessPosition capturePosition = new ChessPosition(row + (up[i] * moveDirection), col + right[i]);
            ChessPiece capturePiece = board.getPiece(capturePosition);
            if ((capturePiece != null) && (piece.getTeamColor() != capturePiece.getTeamColor())) {
                if (canPromote) {
                    ChessPiece.PieceType[] promotionPieces = {ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK};
                    for (ChessPiece.PieceType promote : promotionPieces) {
                        moveList.add(new ChessMove(position, capturePosition, promote));
                    }
                } else {
                    moveList.add(new ChessMove(position, capturePosition, null));
                }
            }
        }

        return moveList;
    }

    static Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        int[] up = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] right = {0, 1, 1, 1, 0, -1, -1, -1};

        for (int i = 0; i < up.length; i++) {
            ChessPosition newPosition = new ChessPosition(row + up[i], col + right[i]);

            while (inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                ChessMove newMove = new ChessMove(position, newPosition, null);

                if (newPiece != null) {
                    if (piece.getTeamColor() != newPiece.getTeamColor()) {
                        moveList.add(newMove);
                    }
                    break;
                }

                moveList.add(newMove);
                newPosition = new ChessPosition(newPosition.getRow() + up[i], newPosition.getColumn() + right[i]);
            }
        }

        return moveList;
    }

    static Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        int[] up = {1, 0, -1, 0};
        int[] right = {0, 1, 0, -1};

        for (int i = 0; i < up.length; i++) {
            ChessPosition newPosition = new ChessPosition(row + up[i], col + right[i]);

            while (inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                ChessMove newMove = new ChessMove(position, newPosition, null);

                if (newPiece != null) {
                    if (piece.getTeamColor() != newPiece.getTeamColor()) {
                        moveList.add(newMove);
                    }
                    break;
                }

                moveList.add(newMove);
                newPosition = new ChessPosition(newPosition.getRow() + up[i], newPosition.getColumn() + right[i]);
            }
        }

        return moveList;
    }
}
