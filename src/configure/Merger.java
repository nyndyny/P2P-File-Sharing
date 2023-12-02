package configure;

import java.io.*;
import java.nio.ByteBuffer;

import peer.PeerProcess;
import messageTypes.Piece;

public class Merger {

    private static final int INT_SIZE = 4;
    private static final int PIECE_DATA_OFFSET = 9;

    public void reassemble(int peerID, long fileSize, long pieceSize, int numPieces) {
        String directory = (new File(System.getProperty("user.dir")).getParent() + "/peer_" + peerID);
        File peerDirectory = new File(directory);

        if (!peerDirectory.exists()) {
            try {
                peerDirectory.mkdir();
            } catch (SecurityException e) {
                System.err.println("Error creating directory: " + e.getMessage());
            }
        }

        String filePath = (directory + "/" + PeerProcess.fName);
        File file = new File(filePath);

        try (OutputStream outputStream = new FileOutputStream(file)) {
            int i = 1;

            while (i <= numPieces - 1) {
                Integer pieceNumber = i;
                Piece piece = PeerProcess.enumPieces.get(pieceNumber);
                byte[] sizeBytes = new byte[INT_SIZE];

                System.arraycopy(piece.payload, 0, sizeBytes, 0, INT_SIZE);
                int size = ByteBuffer.wrap(sizeBytes).getInt();
                size -= INT_SIZE;

                byte[] buffer = new byte[size];
                System.arraycopy(piece.payload, PIECE_DATA_OFFSET, buffer, 0, buffer.length);

                outputStream.write(buffer);
                i++;
            }

            Integer lastPieceNumber = numPieces;
            Piece lastPiece = PeerProcess.enumPieces.get(lastPieceNumber);

            int remainingSize = (int) (fileSize % pieceSize);
            byte[] remainingBuffer = new byte[remainingSize];

            System.arraycopy(lastPiece.payload, PIECE_DATA_OFFSET, remainingBuffer, 0, remainingBuffer.length);
            outputStream.write(remainingBuffer);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
