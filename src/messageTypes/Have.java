package messageTypes;

import java.nio.ByteBuffer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Have {
    private static final int MESSAGE_LENGTH = 5; // 4 bytes for the piece index + 1 byte for the message ID
    private static final byte INDEX_FIELD = 4; // contains a 4-byte piece index field

    private int pieceIndex;
    public byte[] have = new byte[9];
    private byte[] messageLen = new byte[4];
    private byte messageType = 4;
    private byte[] payload = new byte[4];

    public Have(int pieceIndex) { // constructor
        this.pieceIndex = pieceIndex;
        messageLen = ByteBuffer.allocate(4).putInt(4).array();
        System.arraycopy(messageLen, 0, have, 0, messageLen.length);
        payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        System.arraycopy(payload, 0, have, messageLen.length + 1, payload.length);
        have[messageLen.length] = messageType;
    }

    public void send(OutputStream outputStream) throws IOException { // have message send method
        DataOutputStream writeTheData = new DataOutputStream(outputStream);
        // Write the message length
        writeTheData.writeInt(MESSAGE_LENGTH);
        // Write the piece indec field
        writeTheData.writeByte(INDEX_FIELD);
        // Write the piece index
        writeTheData.writeInt(pieceIndex);
        // Flush the output stream
        writeTheData.flush();
    }

    public int getPieceIndex() { // getter method to obtain piece index
        return this.pieceIndex;
    }
}
