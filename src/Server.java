import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Server extends Thread{
	private int peerID;
	private int sPort;

	public Server(int pID, int port) {
		peerID = pID;
		sPort = port;
	}

	public void run() {

		try (ServerSocket listener = new ServerSocket(sPort)) {
			System.out.println("The server is running.");
			int clientNum = 1;

			while (true) {
				new Handler(listener.accept(), peerID).start();
				System.out.println("Client " + clientNum + " is connected!");
				clientNum++;
			}
		} catch (IOException ioException) {
			System.out.println(ioException.toString());
		}
	}

	/**
	 * A handler thread class.  Handlers are spawned from the listening
	 * loop and are responsible for dealing with a single client's requests.
	 */
	private static class Handler extends Thread {
		private String message;    			//message received from the client
		private String MESSAGE;    			//message sent to the client
		private Socket connection;
		private ObjectInputStream in;		//stream read from the socket
		private ObjectOutputStream out;    	//stream write to the socket
		private int peerID;					//The peerID of the 'server'
		private int remoteID;				//The peerID of the 'client'

		public Handler(Socket connection, int peerID ) {
			this.connection = connection;
			this.peerID = peerID;
		}

		public void run() {
 			try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				in = new ObjectInputStream(connection.getInputStream());
				out.flush();

				boolean greeted = false;
				Handshake handshake = new Handshake(peerID);
				sendMessage(handshake.message);
				System.out.println("Handshake sent.");

				while(true) {
					//receive the message sent from the client
					if (!greeted) {
						message = (String) in.readObject();
						if (message.getBytes().length == 32 && message.startsWith(Handshake.header)) {
							System.out.println("Handshake received.");
							greeted = true;

							//TODO: ONE TIME BITFIELD MESSAGES
						}
					} else {
						// TODO: MESSAGES SENT AFTER HANDSHAKE AND BITFIELD
					}
				}
			 } catch(ClassNotFoundException classnot){
				 System.err.println("Data received in unknown format");
			 } catch(IOException ioException){
				System.out.println("Disconnect with Client " + remoteID);
			} finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + remoteID);
				}
			}
		}

		//send a message to the output stream
		public void sendMessage(String msg)
		{
			try{
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
    }
}
