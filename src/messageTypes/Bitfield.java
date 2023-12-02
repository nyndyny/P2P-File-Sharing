package messageTypes;

import java.nio.ByteBuffer;

public class Bitfield {
    public static byte[] bitFieldBytes;
    public static int totalPieces;
    public static boolean isCompleteFile = false;
    public static byte[] payloadBytes;
    public static byte[] messageLengthBytes = new byte[4];
    public static byte messageType = 5;

    public static void setBitfield(boolean fullFile, int numberOfPieces) {
        isCompleteFile = fullFile;
        totalPieces = numberOfPieces;

        int payloadLength = (int) Math.ceil((double) totalPieces / 8);
        int leftoverBits = totalPieces % 8;
        messageLengthBytes = ByteBuffer.allocate(4).putInt(payloadLength).array();
        payloadBytes = new byte[payloadLength];
        bitFieldBytes = new byte[payloadLength + 5];

        int index;
        for (index = 0; index < messageLengthBytes.length; index++) {
            bitFieldBytes[index] = messageLengthBytes[index];
        }

        bitFieldBytes[index] = messageType;

        if (!isCompleteFile) {
            for (int j = 0; j < payloadBytes.length; j++) {
                index++;
                bitFieldBytes[index] = 0; // if file's not present, initialize bitFieldBytes with zero
            }
        } else {
            for (int j = 0; j < payloadBytes.length - 1; j++) {
                index++;
                for (int k = 0; k < 8; k++) {
                    bitFieldBytes[index] = (byte) (bitFieldBytes[index] | (1 << k));
                }
            }

            index++;
            for (int j = 0; j < leftoverBits; j++) {
                bitFieldBytes[index] = (byte) (bitFieldBytes[index] | (1 << (7 - j)));
            }
        }
    }

    public static void updateBitfield(int pieceIndex) {
        int position = pieceIndex - 1;
        bitFieldBytes[(position / 8) + 5] = (byte) (bitFieldBytes[(position / 8) + 5] | (1 << (7 - (position % 8))));
    }
}
