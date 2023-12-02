package messageTypes;

import java.nio.ByteBuffer;

public class NotInterested {
    private byte[] notInterestedMessageBytes;
    private byte[] messageLengthBytes = new byte[4];
    private byte messageType = 3;

    public NotInterested() {
        initializeNotInterestedMessage();
    }

    private void initializeNotInterestedMessage() {
        setMessageLengthBytes(0);
        notInterestedMessageBytes = new byte[messageLengthBytes.length + 1];

        int index;
        for (index = 0; index < messageLengthBytes.length; index++) {
            notInterestedMessageBytes[index] = messageLengthBytes[index];
        }

        notInterestedMessageBytes[index] = messageType;
    }

    private void setMessageLengthBytes(int length) {
        messageLengthBytes = ByteBuffer.allocate(4).putInt(length).array();
    }

    public byte[] getNotInterestedMessageBytes() {
        return notInterestedMessageBytes;
    }
}
