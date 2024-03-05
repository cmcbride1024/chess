import chess.*;
import server.*;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        int port = 8080;
        server.run(port);

        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
    }
}