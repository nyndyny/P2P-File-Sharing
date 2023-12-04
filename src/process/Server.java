package process;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import configure.Logs;
import messageTypes.Bitfield;
import peer.Handshake;
import peer.PeerConnection;
import peer.PeerProcess;
import peer.PeerInfo;

public class Server extends Thread {
	private static final String EXPECTED_HEADER_VALUE = "P2PFILESHARINGPROJ0000000000";

	private int peerID;
	private int sPort;
	private int port;
	private int numPieces;
	private boolean completeFile;
	private long fileSize;
	private long pieceSize;

	public Server(int pID, int port) {
		this.peerID = pID;
		this.sPort = port;
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

				byte[] recvHSContent = receiveHandshake(socket);
				String header = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(header);
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32)).trim();
				int receivedID = Integer.parseInt(peerIDStr);

				Handshake sendHS = new Handshake(peerID);
				sendHandshake(socket, sendHS.content);

				if (header.equals(EXPECTED_HEADER_VALUE)) {
					System.out.println("Header Confirmed.");
					boolean validPeer = isValidPeer(receivedID);

					if (validPeer) {
						handleValidPeer(socket, receivedID);
					} else {
						System.out.println("Unknown Peer Found.");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isValidPeer(int receivedID) {
		for (int tid : PeerProcess.peerIDList) {
			if (tid != peerID && tid == receivedID) {
				return true;
			}
		}
		return false;
	}

	private void handleValidPeer(Socket socket, int receivedID) {
		PeerInfo peer = createPeerInfo(socket, receivedID);

		sendBitField(socket);
		byte[] receivedBitField = receiveBitField(socket);
		peer.setBitfield(receivedBitField);
		peer.setInterested(false);

		PeerProcess.peersList.add(peer);

		PeerConnection peerConnection = new PeerConnection();
		peerConnection.setSocket(socket);
		peerConnection.setCompleteFileDownloaded(false);

		System.out.println("Incoming Connection Request From PeerID: " + receivedID);

		Logs.madeTCPConnection(receivedID);

		Sender messageSender = new Sender();
		messageSender.start();

		RequestPiece pieceRequest = new RequestPiece(receivedID, numPieces, completeFile, fileSize, pieceSize);
		pieceRequest.start();

		Receiver messageReceiver = new Receiver(peerID, receivedID, socket, pieceSize);
		messageReceiver.start();
	}

	private PeerInfo createPeerInfo(Socket socket, int receivedID) {
		PeerInfo peer = new PeerInfo();
		peer.setPersPeerID(peerID);
		peer.setSock(socket);
		peer.setPeerID(receivedID);
		return peer;
	}

	private void sendBitField(Socket socket) {
		try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
			out.writeObject(Bitfield.bitFieldBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendHandshake(Socket socket, byte[] content) {
		try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
			out.writeObject(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] receiveBitField(Socket socket) {
		byte[] bitField = null;
		try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			bitField = (byte[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return bitField;
	}

	private byte[] receiveHandshake(Socket socket) {
		byte[] content = null;
		try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			content = (byte[]) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return content;
	}
}
