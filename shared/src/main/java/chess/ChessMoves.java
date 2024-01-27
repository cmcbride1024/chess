package chess;

import java.util.Collection;
import java.util.HashSet;

public class ChessMoves {
    public static Collection<ChessMove> pieceRules(ChessPiece.PieceType pieceType, ChessBoard board, ChessPosition myPosition) {
        int[] up = null, right = null;
        boolean keepsMoving = false, pawnMoves = false;

        switch(pieceType) {
            case KING:
                up = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
                right = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
                break;
            case QUEEN:
                up = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
                right = new int[]{0, 1, 1, 1, 0, -1, -1, -1};
                keepsMoving = true;
                break;
            case BISHOP:
                up = new int[]{1, -1, -1, 1};
                right = new int[]{1, 1, -1, -1};
                keepsMoving = true;
                break;
            case KNIGHT:
                up = new int[]{2, 1, -1, -2, -2, -1, 1, 2};
                right = new int[]{1, 2, 2, 1, -1, -2, -2, -1};
                break;
            case ROOK:
                up = new int[]{1, 0, -1, 0};
                right = new int[]{0, 1, 0, -1};
                keepsMoving = true;
                break;
            case PAWN:
                // Pawn moves forward or backwards depending on its color
                int dir = (board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1);
                up = new int[]{dir, dir};
                right = new int[]{1, -1};
                pawnMoves = true;
                break;
        }

        return pieceMoves(board, myPosition, up, right, keepsMoving, pawnMoves);
    }

    private static boolean inBounds(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private static boolean handleMove(Collection<ChessMove> moveList, ChessPiece piece, ChessPiece newPiece, ChessPosition position, ChessPosition newPosition, boolean promotion) {
        ChessPiece.PieceType[] promotionPieces = {ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK};
        boolean isPawn = (piece.getPieceType() == ChessPiece.PieceType.PAWN);

        // Determines if an enemy's piece is in a potential new position
        if (newPiece != null && piece.getTeamColor() != newPiece.getTeamColor()) {

            // If the piece is a pawn ready to be promoted
            if (promotion) {
                for (ChessPiece.PieceType promotionPiece : promotionPieces) {
                    moveList.add(new ChessMove(position, newPosition, promotionPiece));
                }
            }

            // Every other piece scenario
            else {
                moveList.add(new ChessMove(position, newPosition, null));
            }
            return true;
        }

        // If the piece is a pawn or one of your own pieces is in a potential new position
        else if (isPawn || newPiece != null) {
            return true;
        }

        // No piece at a potential new position
        moveList.add(new ChessMove(position, newPosition, null));
        return false;
    }


    private static Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position, int[] up, int[] right, boolean keepsMoving, boolean pawnMoves) {
        Collection<ChessMove> moveList = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessPiece piece = board.getPiece(position);
        boolean promotion = pawnMoves && ((row == 7 && piece.getTeamColor() == ChessGame.TeamColor.WHITE) || (row == 2 && piece.getTeamColor() == ChessGame.TeamColor.BLACK));

        for (int i = 0; i < up.length; i++) {
            ChessPosition newPosition = new ChessPosition(row + up[i], col + right[i]);

            while (keepsMoving && inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                boolean stopMoving = handleMove(moveList, piece, newPiece, position, newPosition, promotion);
                newPosition = new ChessPosition(newPosition.getRow() + up[i], newPosition.getColumn() + right[i]);
                if (stopMoving) break;
            }

            if (!keepsMoving && inBounds(newPosition)) {
                ChessPiece newPiece = board.getPiece(newPosition);
                handleMove(moveList, piece, newPiece, position, newPosition, promotion);
            }
        }

        // Handles extra logic for pawns
        if (pawnMoves) {
            int dir = (piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1);
            ChessPosition forwardPosition = new ChessPosition(row + dir, col);
            ChessPiece forwardPiece = board.getPiece(forwardPosition);

            // Making sure no piece is in front of the pawn
            if (forwardPiece == null) {
                boolean extraMove = (row == 2 && piece.getTeamColor() == ChessGame.TeamColor.WHITE) || (row == 7 && piece.getTeamColor() == ChessGame.TeamColor.BLACK);

                // If the pawn hasn't moved yet
                if (extraMove) {
                    ChessPosition extraPosition = new ChessPosition(row + (2 * dir), col);
                    ChessPiece extraPiece = board.getPiece(extraPosition);

                    // Making sure no piece is preventing extra move
                    if (extraPiece == null) {
                        moveList.add(new ChessMove(position, extraPosition, null));
                    }
                }

                // If the pawn will move to the promotion row
                if (promotion) {
                    ChessPiece.PieceType[] promotionPieces = {ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.ROOK};
                    for (ChessPiece.PieceType promotionPiece : promotionPieces) {
                        moveList.add(new ChessMove(position, forwardPosition, promotionPiece));
                    }
                }

                // If the pawn is anywhere else
                else {
                    moveList.add(new ChessMove(position, forwardPosition, null));
                }
            }
        }
        return moveList;
    }
}
