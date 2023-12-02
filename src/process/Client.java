package process;

import java.net.*;
import java.io.*;
import java.util.*;

import peer.Handshake;
import peer.PeerInfo;

public class Client extends Thread {
	private int peerID;
	private Vector<PeerInfo> remotePeers;

	public Client(int pID, Vector<PeerInfo> rPeers) {
		peerID = pID;
		remotePeers = rPeers;
	}

	public void run() {
		for (PeerInfo peer : remotePeers) {
			try {
				new ServerHandler(new Socket(peer.peerAddress, peer.peerPort), peer.peerID).start();
				System.out.println("Connected to '" + peer.peerAddress + "' in port " + peer.peerPort + ".");
			} catch (IOException ioException) {
				System.out.println(ioException.toString());
			}
		}

	}

	public class ServerHandler extends Thread {
		private Socket requestSocket; // socket connect to the server
		private ObjectOutputStream out; // stream write to the socket
		private ObjectInputStream in; // stream read from the socket
		private String message; // message send to the server
		private String MESSAGE; // capitalized message read from the server
		private String address;
		private int remoteID;

		public ServerHandler(Socket reqSocket, int rID) {
			requestSocket = reqSocket;
			remoteID = rID;
		}

		public void run() {
			try {
				// create a socket to connect to the server

				// initialize inputStream and outputStream
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				in = new ObjectInputStream(requestSocket.getInputStream());
				out.flush();

				boolean greeted = false;
				Handshake handshake = new Handshake(peerID);
				sendMessage(handshake.message);

				System.out.println("Handshake sent.");

				try {
					while (true) {
						// receive the message sent from the client
						if (!greeted) {
							message = (String) in.readObject();

							if (message.getBytes().length == 32 && message.startsWith(Handshake.header)) {
								System.out.println("Handshake received.");
								greeted = true;

								// TODO: ONE TIME BITFIELD MESSAGES
							}
						} else {
							// TODO: MESSAGES SENT AFTER HANDSHAKE AND BITFIELD
						}
					}
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} catch (ConnectException e) {
				System.err.println("Connection refused. You need to initiate a server first.");
			} catch (UnknownHostException unknownHost) {
				System.err.println("You are trying to connect to an unknown host!");
			} catch (IOException ioException) {
				ioException.printStackTrace();
			} finally {
				// Close connections
				try {
					in.close();
					out.close();
					requestSocket.close();
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}

		// send a message to the output stream
		void sendMessage(String msg) {
			try {
				// stream write the message
				out.writeObject(msg);
				out.flush();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
}
