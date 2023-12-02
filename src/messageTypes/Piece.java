package messageTypes;

import java.nio.ByteBuffer;

public class Piece {
    private byte[] payload;
    private byte[] lengthBytes = new byte[4];
    private byte messageType = 7;
    private byte[] indexBytes = new byte[4];
    private byte[] assembledMessage;

    // Constructor
    public Piece(int index, byte[] payloadData) {
        payload = payloadData;
        int payloadLength = payload.length;
        indexBytes = ByteBuffer.allocate(4).putInt(index).array();
        lengthBytes = ByteBuffer.allocate(4).putInt(4 + payloadLength).array();

        assembleMessage();
    }

    private void assembleMessage() {
        assembledMessage = new byte[9 + payload.length];

        System.arraycopy(lengthBytes, 0, assembledMessage, 0, lengthBytes.length);

        int position = lengthBytes.length;
        assembledMessage[position++] = messageType;

        System.arraycopy(indexBytes, 0, assembledMessage, position, indexBytes.length);
        position += indexBytes.length;

        System.arraycopy(payload, 0, assembledMessage, position, payload.length);
    }
}
