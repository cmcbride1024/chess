package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
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
     * @return Boolean indicating if position array
     * is in bounds
     */
    private boolean inBounds(int[] pos) {
        return pos[0] >= 1 && pos[0] <= 8 && pos[1] >= 1 && pos[1] <= 8;
    }

    /**
     * @return Collection of valid moves for a bishop
     */
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[] up = {1, -1, -1, 1};
        int[] right = {1, 1, -1, -1};
        for (int i = 0; i < up.length; i++) {
            int[] currentPosition = {row + up[i], col + right[i]};
            while (inBounds(currentPosition)) {
                ChessPosition newPosition = new ChessPosition(currentPosition[0], currentPosition[1]);
                ChessMove newMove = new ChessMove(myPosition, newPosition,null);
                moveList.add(newMove);
                currentPosition = new int[]{currentPosition[0] + up[i], currentPosition[1] + right[i]};
            }
        }
        return moveList;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moveList = new HashSet<>();
        PieceType piece = getPieceType();
        switch(piece) {
            case BISHOP:
                moveList = bishopMoves(board, myPosition);
                break;
        }
        return moveList;
    }
}
