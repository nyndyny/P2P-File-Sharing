package peer;
/*
    specified by Page 1 of Project Description
    The handshake consists of three parts: handshake header, zero bits, and peer ID. The 
    length of the handshake message is 32 bytes. The handshake header is 18-byte string 
    ‘P2PFILESHARINGPROJ’, which is followed by 10-byte zero bits, which is followed by 
    4-byte peer ID which is the integer representation of the peer ID. 
*/

import java.nio.charset.StandardCharsets;

public class Handshake {
    public static String header = "P2PFILESHARINGPROJ" + "0000000000";
    public String message;
    public byte[] content = new byte[32];

    public Handshake(int pID) {
        message = header + Integer.toString(pID);
        content = message.getBytes();
    }
}
