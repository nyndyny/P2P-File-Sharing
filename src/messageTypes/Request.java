package messageTypes;

import java.nio.ByteBuffer;

public class Request {
    private byte[] lengthBytes = new byte[4];
    private byte messageType = 6;
    private byte[] indexBytes = new byte[4];
    public byte[] requestMessage = new byte[9];

    // Constructor
    public Request(int index) {
        lengthBytes = ByteBuffer.allocate(4).putInt(4).array();
        indexBytes = ByteBuffer.allocate(4).putInt(index).array();

        assembleRequestMessage();
    }

    private void assembleRequestMessage() {
        System.arraycopy(lengthBytes, 0, requestMessage, 0, lengthBytes.length);

        int position = lengthBytes.length;
        requestMessage[position++] = messageType;

        System.arraycopy(indexBytes, 0, requestMessage, position, indexBytes.length);
    }
}
