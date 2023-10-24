package messageTypes;

import java.nio.ByteBuffer;

public class Choke
{
    public byte[] chokeMessage = new byte[5];
    public byte[] msgLength = new byte[4];

    public Choke()
    {
        msgLength = ByteBuffer.allocate(4).putInt(0).array();
        int i;
        for(i = 0; i < msgLength.length; i++)
        {
            chokeMessage[i] = msgLength[i];
        }
        chokeMessage[i] = 0; //0 -> choke message type
    }
}
