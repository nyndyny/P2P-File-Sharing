package messageTypes;

import java.nio.ByteBuffer;

public class Interested {
    private byte[] interestedMessageBytes;
    private byte[] messageLengthBytes = new byte[4];
    private byte messageType = 2;

    public Interested() {
        initializeInterestedMessage();
    }

    private void initializeInterestedMessage() {
        setMessageLengthBytes(0);
        interestedMessageBytes = new byte[messageLengthBytes.length + 1];

        int index;
        for (index = 0; index < messageLengthBytes.length; index++) {
            interestedMessageBytes[index] = messageLengthBytes[index];
        }

        interestedMessageBytes[index] = messageType;
    }

    private void setMessageLengthBytes(int length) {
        messageLengthBytes = ByteBuffer.allocate(4).putInt(length).array();
    }

    public byte[] getInterestedMessageBytes() {
        return interestedMessageBytes;
    }
}
