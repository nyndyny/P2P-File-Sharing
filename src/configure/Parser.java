package configure;

import java.io.*;
import java.util.HashMap;
import messageTypes.Piece;

public class Parser {
    private int peerId;
    private long pieceSize;
    private HashMap<Integer, Piece> piecesMap = new HashMap<>();
    private int currentPieceNumber = 1;
    private String fileName;

    public Parser(int id, String name, long size) {
        this.peerId = id;
        this.pieceSize = size;
        this.fileName = name;
    }

    public HashMap<Integer, Piece> readPiecesFromFile() {
        String filePath = new File(System.getProperty("user.dir")).getParent() + "/peer_" + peerId + "/" + fileName;
        File file = new File(filePath);

        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) pieceSize];

            int bytesRead = inputStream.read(buffer);
            Piece currentPiece;
            while (bytesRead > 0) {
                currentPiece = new Piece(currentPieceNumber, buffer);
                piecesMap.put(currentPieceNumber, currentPiece);
                currentPieceNumber++;
                bytesRead = inputStream.read(buffer);
            }

        } catch (FileNotFoundException fileNotFoundException) {
            System.err.println("The file was not found: " + fileNotFoundException.getMessage());
        } catch (IOException ioException) {
            System.err.println("There was an error reading this file: " + ioException.getMessage());
        }

        return piecesMap;
    }
}
