package process;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

import peer.Handshake;
import peer.PeerInfo;

import configure.Logs;
import configure.PeerInfoParser;
import peer.PeerConnection;
import peer.PeerProcess;
import messageTypes.Bitfield;

public class Client extends Thread {
	private int peerID;
	private int port;
	private String peerIP;
	private ArrayList<String[]> arr = new ArrayList<String[]>();
	private int numPieces;
	private long fileSize;
	private long pieceSize;
	private boolean completeFile;
	private String EXPECTED_HEADER_VALUE = "P2PFILESHARINGPROJ0000000000";

	private Vector<PeerInfo> remotePeers;

	public Client(int pID, Vector<PeerInfo> rPeers) {
		peerID = pID;
		remotePeers = rPeers;
	}

	public Client(int peerID, boolean completeFile, int nPieces, long fSize, long pSize) {

		this.peerID = peerID;
		this.fileSize = fSize;
		this.pieceSize = pSize;
		this.numPieces = nPieces;
		this.completeFile = completeFile;

	}

	@Override
	public void run() {
		PeerInfoParser peerInfoParser = new PeerInfoParser(peerID);
		arr = peerInfoParser.getAllPeerInfo();
		Iterator<String[]> itr = arr.iterator();

		while (itr.hasNext()) {
			String[] infoArr = itr.next();
			peerIP = infoArr[1];
			port = Integer.parseInt(infoArr[2]);

			try {
				System.out.println("Inside client " + peerIP);
				Socket sock = new Socket(peerIP, port);
				Handshake sendHS = new Handshake(peerID);
				handShakeSender(sock, sendHS.content);
				System.out.println("Sent Handshake.");

				// Receiving handshake from recently connected peer
				byte[] recvHSContent = handShakeReceiver(sock);
				// Extracting head from content
				String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
				System.out.println(head);
				// Extracting peer id from content
				String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32)).trim();
				int rcvdID = Integer.parseInt(peerIDStr);

				// Check header value
				if (head.equals(EXPECTED_HEADER_VALUE)) {
					System.out.println("Head confirmed.");
					boolean flag = false;
					Iterator<Integer> itr2 = PeerProcess.peerIDList.iterator();

					// Checking for ID in handshake content
					while (itr2.hasNext()) {
						int tid = itr2.next();

						if (tid != peerID && tid == rcvdID) {
							flag = true;
							break;
						}
					}

					if (flag) {
						// Populate Peer Object with appropriate values.
						PeerInfo peer = new PeerInfo();
						peer.setPersPeerID(peerID);
						peer.setSock(sock);
						peer.setPeerID(Integer.parseInt(infoArr[0]));

						// Receive bitfield from the peer
						byte[] rcvdField = recieveBitfield(sock);
						peer.setBitfield(rcvdField);

						// Send bitfield of available pieces to connected peer
						sendBitfield(sock);
						peer.setInterested(false);

						synchronized (PeerProcess.peersList) {
							PeerProcess.peersList.add(peer);
							Thread.sleep(1);
						}

						PeerConnection completeFile = new PeerConnection();
						completeFile.setSocket(sock);
						completeFile.setCompleteFileDownloaded(false);

						PeerProcess.hasFullFile.add(completeFile);

						System.out.println("Requesting Connection to PeerID: " + Integer.parseInt(infoArr[0]));
						System.out.println();
						Logs.makingTCPConnection(Integer.parseInt(infoArr[0]));

						Sender ms = new Sender();
						ms.start();

						RequestPiece pr = new RequestPiece(Integer.parseInt(infoArr[0]), numPieces, this.completeFile,
								fileSize, pieceSize);
						pr.start();

						Receiver mr = new Receiver(peerID, rcvdID, sock, pieceSize);
						mr.start();
					} else {
						System.out.println("Unknown Peer Found.");
					}
				}

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
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

	private byte[] recieveBitfield(Socket sock) {
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

	private void handShakeSender(Socket sock, byte[] content) {
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendBitfield(Socket sock) {

		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(Bitfield.bitFieldBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// public void run() {
	// for (PeerInfo peer : remotePeers) {
	// try {
	// new ServerHandler(new Socket(peer.peerAddress, peer.peerPort),
	// peer.peerID).start();
	// System.out.println("Connected to '" + peer.peerAddress + "' in port " +
	// peer.peerPort + ".");
	// } catch (IOException ioException) {
	// System.out.println(ioException.toString());
	// }
	// }

	// }

	// public class ServerHandler extends Thread {
	// private Socket requestSocket; // socket connect to the server
	// private ObjectOutputStream out; // stream write to the socket
	// private ObjectInputStream in; // stream read from the socket
	// private String message; // message send to the server
	// private String MESSAGE; // capitalized message read from the server
	// private String address;
	// private int remoteID;

	// public ServerHandler(Socket reqSocket, int rID) {
	// requestSocket = reqSocket;
	// remoteID = rID;
	// }

	// public void run() {
	// try {
	// // create a socket to connect to the server

	// // initialize inputStream and outputStream
	// out = new ObjectOutputStream(requestSocket.getOutputStream());
	// in = new ObjectInputStream(requestSocket.getInputStream());
	// out.flush();

	// boolean greeted = false;
	// Handshake handshake = new Handshake(peerID);
	// sendMessage(handshake.message);

	// System.out.println("Handshake sent.");

	// try {
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
	// }
	// } catch (ConnectException e) {
	// System.err.println("Connection refused. You need to initiate a server
	// first.");
	// } catch (UnknownHostException unknownHost) {
	// System.err.println("You are trying to connect to an unknown host!");
	// } catch (IOException ioException) {
	// ioException.printStackTrace();
	// } finally {
	// // Close connections
	// try {
	// in.close();
	// out.close();
	// requestSocket.close();
	// } catch (IOException ioException) {
	// ioException.printStackTrace();
	// }
	// }
	// }

	// // send a message to the output stream
	// void sendMessage(String msg) {
	// try {
	// // stream write the message
	// out.writeObject(msg);
	// out.flush();
	// } catch (IOException ioException) {
	// ioException.printStackTrace();
	// }
	// }
	// }
}
