package messageTypes;

import java.nio.ByteBuffer;

public class Unchoke
{
	public byte[] unchokeMsg = new byte[5];
	public byte[] msgLen = new byte[4];
	
	public Unchoke()
	{
		msgLen = ByteBuffer.allocate(4).putInt(0).array(); 
		int i;
		for(i = 0; i < msgLen.length; i++)
		{
			unchokeMsg[i] = msgLen[i];
		}
		unchokeMsg[i] = 1; //1 -> unchoke message type
	}
}
