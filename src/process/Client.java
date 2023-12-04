package process;

import configure.Logs;
import configure.PeerInfoParser;
import messageTypes.Bitfield;
import peer.Handshake;
import peer.PeerConnection;
import peer.PeerInfo;
import peer.PeerProcess;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class Client extends Thread {
	private static final String EXPECTED_HEADER_VALUE = "P2PFILESHARINGPROJ0000000000";

	private int peerID;
	private int port;
	private String peerIP;
	private ArrayList<String[]> arr = new ArrayList<>();
	private int numPieces;
	private long fileSize;
	private long pieceSize;
	private boolean completeFile;

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
			Socket sock = null;

			try {
				System.out.println("Inside client " + peerIP);
				sock = new Socket(peerIP, port);
				performHandshake(sock);

				byte[] recvHSContent = receiveHandshake(sock);
				processReceivedHandshake(recvHSContent, infoArr, sock);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (sock != null && !sock.isClosed()) {
					try {
						sock.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void performHandshake(Socket sock) throws IOException {
		Handshake sendHS = new Handshake(peerID);
		handShakeSender(sock, sendHS.content);
		System.out.println("Sent Handshake.");
	}

	private byte[] receiveHandshake(Socket sock) {
		byte[] content = null;
		try {
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			content = (byte[]) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private void processReceivedHandshake(byte[] recvHSContent, String[] infoArr, Socket sock) {
		String head = new String(Arrays.copyOfRange(recvHSContent, 0, 28), StandardCharsets.UTF_8);
		System.out.println(head);

		String peerIDStr = new String(Arrays.copyOfRange(recvHSContent, 28, 32)).trim();
		int rcvdID = Integer.parseInt(peerIDStr);

		if (head.equals(EXPECTED_HEADER_VALUE) && isRemotePeerValid(rcvdID)) {
			System.out.println("Head confirmed.");
			PeerInfo peer = createPeerInfo(infoArr, rcvdID, sock);

			startCommunicationThreads(infoArr, sock, peer);

		} else {
			System.out.println("Unknown Peer Found.");
		}
	}

	private boolean isRemotePeerValid(int rcvdID) {
		return PeerProcess.peerIDList.stream().anyMatch(tid -> tid != peerID && tid == rcvdID);
	}

	private PeerInfo createPeerInfo(String[] infoArr, int rcvdID, Socket sock) {
		PeerInfo peer = new PeerInfo();
		peer.setPersPeerID(peerID);
		peer.setSock(sock);
		peer.setPeerID(Integer.parseInt(infoArr[0]));

		byte[] rcvdField = recieveBitfield(sock);
		peer.setBitfield(rcvdField);

		sendBitfield(sock);
		peer.setInterested(false);

		synchronized (PeerProcess.peersList) {
			PeerProcess.peersList.add(peer);
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		PeerConnection completeFile = new PeerConnection();
		completeFile.setSocket(sock);
		completeFile.setCompleteFileDownloaded(false);

		PeerProcess.hasFullFile.add(completeFile);

		System.out.println("Requesting Connection to PeerID: " + Integer.parseInt(infoArr[0]));
		System.out.println();
		Logs.makingTCPConnection(Integer.parseInt(infoArr[0]));

		return peer;
	}

	private void startCommunicationThreads(String[] infoArr, Socket sock, PeerInfo peer) {
		Sender ms = new Sender();
		ms.start();

		RequestPiece pr = new RequestPiece(Integer.parseInt(infoArr[0]), numPieces, this.completeFile, fileSize,
				pieceSize);
		pr.start();

		Receiver mr = new Receiver(peerID, peer.getPeerID(), sock, pieceSize);
		mr.start();
	}

	private byte[] recieveBitfield(Socket sock) {
		byte[] bf = null;
		try (ObjectInputStream in = new ObjectInputStream(sock.getInputStream())) {
			bf = (byte[]) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return bf;
	}

	private void handShakeSender(Socket sock, byte[] content) {
		try (ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())) {
			out.writeObject(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendBitfield(Socket sock) {
		try (ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream())) {
			out.writeObject(Bitfield.bitFieldBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
