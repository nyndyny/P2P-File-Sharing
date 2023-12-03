package process;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import configure.Logs;
import messageTypes.Bitfield;
import peer.Handshake;
import peer.PeerConnection;
import peer.PeerProcess;
import peer.PeerInfo;

public class Server extends Thread {
	private int peerID;
	private int sPort;
	private int port;
	private int numPieces;
	private boolean completeFile;
	private long fileSize;
	private long pieceSize;
	private String EXPECTED_HEADER_VALUE = "P2PFILESHARINGPROJ0000000000";

	public Server(int pID, int port) {
		peerID = pID;
		sPort = port;
	}

	public Server(int PORT, int peerID, boolean completeFile, int nPieces, long fSize, long pSize) {
		this.port = PORT;
		this.peerID = peerID;
		this.completeFile = completeFile;
		this.numPieces = nPieces;
		this.fileSize = fSize;
		this.pieceSize = pSize;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("Server is running on " + port);

			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("Accepted Connection");

				// Receiving handshake from recently connected peer
				byte[] recvHSContent = handShakeReceiver(socket);
				// Extracting head from content
				String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(head);
				// Extracting peer id from content
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32)).trim();
				int rcvdID = Integer.parseInt(peerIDStr);

				// Sending Servers handshake to connected peer
				Handshake sendHS = new Handshake(peerID);
				handShakeSender(socket, sendHS.content);

				// Check header value
				if (head.equals(EXPECTED_HEADER_VALUE)) {
					System.out.println("Head Confirmed.");
					boolean flag = false;

					for (int tid : PeerProcess.peerIDList) {
						if (tid != peerID && tid == rcvdID) {
							flag = true;
							break;
						}
					}

					if (flag) {
						PeerInfo peer = new PeerInfo();
						peer.setPersPeerID(this.peerID);
						peer.setSock(socket);
						peer.setPeerID(rcvdID);

						sendBitField(socket);

						byte[] rcvdField = recieveBitField(socket);
						peer.setBitfield(rcvdField);

						peer.setInterested(false);

						// synchronized (PeerProcess.peersList) {
						PeerProcess.peersList.add(peer);
						// }

						PeerConnection completeFile = new PeerConnection();
						completeFile.setSocket(socket);
						completeFile.setCompleteFileDownloaded(false);

						System.out.println("Incoming Connection Request From PeerID: " + rcvdID);

						Logs.madeTCPConnection(rcvdID);

						// Collect any messages present in the message pool and send them
						// in a synchronous fashion
						Sender ms = new Sender();
						ms.start();

						// Keep requesting new pieces, PieceRequest program will terminate
						// program thread if all peers finish downloading.
						RequestPiece pr = new RequestPiece(rcvdID, numPieces, this.completeFile, fileSize, pieceSize);
						pr.start();

						// Receive & Process messages by type
						Receiver mr = new Receiver(peerID, rcvdID, socket, pieceSize);
						mr.start();
					} else {
						System.out.println("Unknown Peer Found.");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendBitField(Socket sock) {

		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(Bitfield.bitFieldBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handShakeSender(Socket sock, byte[] content) {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] recieveBitField(Socket sock) {
		byte[] bf = null;
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(sock.getInputStream());
			bf = (byte[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return bf;
	}

	private byte[] handShakeReceiver(Socket sock) {
		byte[] content = null;
		try {
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			content = (byte[]) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	// public void run() {

	// try (ServerSocket listener = new ServerSocket(sPort)) {
	// System.out.println("The server is running.");
	// int clientNum = 1;

	// while (true) {
	// new Handler(listener.accept(), peerID).start();
	// System.out.println("Client " + clientNum + " is connected!");
	// clientNum++;
	// }
	// } catch (IOException ioException) {
	// System.out.println(ioException.toString());
	// }
	// }

	// /**
	// * A handler thread class. Handlers are spawned from the listening
	// * loop and are responsible for dealing with a single client's requests.
	// */
	// private static class Handler extends Thread {
	// private String message; // message received from the client
	// private String MESSAGE; // message sent to the client
	// private Socket connection;
	// private ObjectInputStream in; // stream read from the socket
	// private ObjectOutputStream out; // stream write to the socket
	// private int peerID; // The peerID of the 'server'
	// private int remoteID; // The peerID of the 'client'

	// public Handler(Socket connection, int peerID) {
	// this.connection = connection;
	// this.peerID = peerID;
	// }

	// public void run() {
	// try {
	// // initialize Input and Output streams
	// out = new ObjectOutputStream(connection.getOutputStream());
	// in = new ObjectInputStream(connection.getInputStream());
	// out.flush();

	// boolean greeted = false;
	// Handshake handshake = new Handshake(peerID);
	// sendMessage(handshake.message);
	// System.out.println("Handshake sent.");

	// while (true) {
	// // receive the message sent from the client
	// if (!greeted) {
	// message = (String) in.readObject();
	// if (message.getBytes().length == 32 && message.startsWith(Handshake.header))
	// {
	// System.out.println("Handshake received.");
	// greeted = true;

	// // TODO: ONE TIME BITFIELD MESSAGES
	// }
	// } else {
	// // TODO: MESSAGES SENT AFTER HANDSHAKE AND BITFIELD
	// }
	// }
	// } catch (ClassNotFoundException classnot) {
	// System.err.println("Data received in unknown format");
	// } catch (IOException ioException) {
	// System.out.println("Disconnect with Client " + remoteID);
	// } finally {
	// // Close connections
	// try {
	// in.close();
	// out.close();
	// connection.close();
	// } catch (IOException ioException) {
	// System.out.println("Disconnect with Client " + remoteID);
	// }
	// }
	// }

	// // send a message to the output stream
	// public void sendMessage(String msg) {
	// try {
	// out.writeObject(msg);
	// out.flush();
	// } catch (IOException ioException) {
	// ioException.printStackTrace();
	// }
	// }
	// }
}
