package peer;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Message {
    int length;
    char type;
    String payload = null;
    private Socket socket;
    private byte[] message;

    public Message(char msgtype, String load) {
        type = msgtype;
        payload = load;
        length = payload.length() + 1;
    }

    public Message(char msgtype) {
        length = 1;
        type = msgtype;
    }

    public Message() {
    }

    public Socket getSock() {
        return socket;
    }

    public byte[] getMessage() {
        return message;
    }

    public void setSock(Socket socket) {
        this.socket = socket;
    }

    public void setMessage(byte[] Message) {
        this.message = Message;
    }
}
