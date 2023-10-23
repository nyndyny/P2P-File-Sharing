import java.nio.charset.StandardCharsets;

public class Message {
    int length;
    char type;
    String payload = null;

    public Message(char msgtype, String load){
        type = msgtype;
        payload = load;
        length = payload.length() + 1;
    }

    public Message(char msgtype){
        length = 1;
        type = msgtype;
    }
}
