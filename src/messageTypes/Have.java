package messageTypes;

import java.nio.ByteBuffer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

<<<<<<< HEAD
<<<<<<< HEAD:src/messageTypes/Have.java
public class Have {
=======
public class haveMessage {
>>>>>>> c2f3a771f6cb3937c217bd06545fe0a26d0e4461:src/messageTypes/haveMessage.java
=======
public class Have {
>>>>>>> e6cd97a947636cad301811958c720293d94a8660
    private static final int MESSAGE_LENGTH = 5; // 4 bytes for the piece index + 1 byte for the message ID
    private static final byte INDEX_FIELD = 4; // contains a 4-byte piece index field

    private int pieceIndex;

<<<<<<< HEAD
<<<<<<< HEAD:src/messageTypes/Have.java
    public Have(int pieceIndex) { // constructor
=======
    public haveMessage(int pieceIndex) { // constructor
>>>>>>> c2f3a771f6cb3937c217bd06545fe0a26d0e4461:src/messageTypes/haveMessage.java
=======
    public Have(int pieceIndex) { // constructor
>>>>>>> e6cd97a947636cad301811958c720293d94a8660
        this.pieceIndex = pieceIndex;
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
