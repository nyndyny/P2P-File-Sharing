package process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Iterator;

import configure.Logs;
import peer.PeerConnection;
import peer.Message;
import peer.PeerInfo;
import peer.PeerProcess;
import messageTypes.Bitfield;
import messageTypes.Have;
import messageTypes.Piece;

public class Receiver extends Thread {

    private Socket socket;
    private int remotePeerID;
    private long pieceSize;

    public Receiver(int localPeerID, int remotePeerID, Socket socket, long pieceSize) {
        this.socket = socket;
        this.pieceSize = pieceSize;
        this.remotePeerID = remotePeerID;
    }

    @Override
    public void run() {
        while (true) {
            byte[] message = receiveMessage();
            int messageType = message[4];

            switch (messageType) {
                case 0:
                    handleChokeMessage();
                    break;
                case 1:
                    handleUnchokeMessage();
                    break;
                case 2:
                    handleInterestedMessage();
                    break;
                case 3:
                    handleNotInterestedMessage();
                    break;
                case 4:
                    handleHaveMessage(message);
                    break;
                case 6:
                    handleRequestMessage(message);
                    break;
                case 7:
                    handlePieceMessage(message);
                    break;
                case 8:
                    handleFullFileSignal();
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected value: " + messageType);
            }
        }
    }

    private void handleChokeMessage() {
        System.out.println("Incoming Choke Message from Peer: " + remotePeerID);
        System.out.println();
        Logs.choking(remotePeerID);
    }

    private void handleUnchokeMessage() {
        System.out.println("Incoming Unchoke Message from Peer: " + remotePeerID);
        System.out.println();
        Logs.unchoking(remotePeerID);
    }

    private void handleInterestedMessage() {
        System.out.println("Incoming Interested Message from Peer: " + remotePeerID);
        System.out.println();
        Logs.receivingInterested(remotePeerID);
    }

    private void handleNotInterestedMessage() {
        System.out.println("Incoming Not Interested Message from Peer: " + remotePeerID);
        System.out.println();
        Logs.receivingNotInterested(remotePeerID);
    }

    private void handleHaveMessage(byte[] message) {
        int pieceIndex = ByteBuffer.wrap(message, 5, 4).getInt();

        System.out.println("Have message received " + remotePeerID + " for piece " + pieceIndex);
        System.out.println();
        Logs.receivingHave(remotePeerID, pieceIndex);
    }

    private void handleRequestMessage(byte[] message) {
        int pieceIndex = ByteBuffer.wrap(message, 5, 4).getInt();
        Integer index = pieceIndex;
        Piece piece = PeerProcess.enumPieces.get(index);

        synchronized (PeerProcess.msgPool) {
            Message msg = new Message();
            msg.setSock(socket);
            msg.setMessage(piece.payload);
            PeerProcess.msgPool.add(msg);
        }
    }

    private void handlePieceMessage(byte[] message) {
        int pieceIndex = ByteBuffer.wrap(message, 5, 4).getInt();
        Integer index = pieceIndex;

        byte[] data = new byte[message.length - 9];
        for (int i = 0, j = 9; i < data.length; i++, j++) {
            data[i] = message[j];
        }

        if (data.length == pieceSize && !PeerProcess.enumPieces.containsKey(index)) {
            Piece piece = new Piece(pieceIndex, data);

            synchronized (PeerProcess.enumPieces) {
                PeerProcess.enumPieces.put(index, piece);
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Piece " + pieceIndex + " received from " + remotePeerID);
        System.out.println();
        Logs.downloadingPiece(remotePeerID, pieceIndex);

        try {
            synchronized (Bitfield.bitFieldBytes) {
                Bitfield.updateBitfield(pieceIndex);
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Have have = new Have(pieceIndex);
        Iterator<PeerInfo> iterator = PeerProcess.peersList.iterator();

        while (iterator.hasNext()) {
            PeerInfo peer = iterator.next();

            synchronized (PeerProcess.msgPool) {
                Message msg = new Message();
                msg.setSock(peer.getSock());
                msg.setMessage(have.have);
                PeerProcess.msgPool.add(msg);
            }
        }
    }

    private void handleFullFileSignal() {
        synchronized (PeerProcess.hasFullFile) {
            Iterator<PeerConnection> iterator = PeerProcess.hasFullFile.iterator();
            while (iterator.hasNext()) {
                PeerConnection completeFile = iterator.next();
                if (completeFile.getSocket().equals(socket)) {
                    completeFile.setCompleteFileDownloaded(true);
                    break;
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] receiveMessage() {
        byte[] message = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            message = (byte[]) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exiting now.");
            System.exit(0);
        }
        return message;
    }
}
