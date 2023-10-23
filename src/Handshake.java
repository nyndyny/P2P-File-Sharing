import java.nio.charset.StandardCharsets;

public class Handshake {
    public static String header = "P2PFILESHARINGPROJ" + "0000000000";
    public String message;

    public Handshake(int pID) {
        message = header + Integer.toString(pID);
    }
}
