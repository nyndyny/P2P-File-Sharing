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
        byte[] pieceInfo = new byte[4];
        for (int i = 0, k = 5; i < pieceInfo.length; i++, k++) {
            pieceInfo[i] = message[k];
        }

        int pieceIndex = ByteBuffer.wrap(pieceInfo).getInt();
        Iterator<PeerInfo> pItr = PeerProcess.peersList.iterator();

        while (pItr.hasNext()) {
            PeerInfo p = (PeerInfo) pItr.next();

            if (p.getSock().equals(socket)) {
                byte[] bField = p.getBitfield();

                try {
                    synchronized (bField) {
                        bField = updateBitField(bField, pieceIndex);
                        p.setBitfield(bField);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

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
            msg.setMessage(piece.assembledMessage);
            PeerProcess.msgPool.add(msg);
        }
    }

    private void handlePieceMessage(byte[] message) {
        int pieceIndex = ByteBuffer.wrap(message, 5, 4).getInt();
        Integer index = pieceIndex;

        byte[] data = extractDataFromMessage(message);

        if (isValidPieceData(pieceIndex, data)) {
            Piece piece = createPiece(pieceIndex, data);

            synchronized (PeerProcess.enumPieces) {
                PeerProcess.enumPieces.put(index, piece);
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Piece " + pieceIndex + " received from " + remotePeerID);
            System.out.println();
            Logs.downloadingPiece(remotePeerID, pieceIndex);

            updateBitfield(pieceIndex);

            sendHaveMessage(pieceIndex);
        }
    }

    private byte[] extractDataFromMessage(byte[] message) {
        int dataStartIndex = 9;
        int dataLength = message.length - dataStartIndex;
        byte[] data = new byte[dataLength];

        for (int i = 0, j = dataStartIndex; i < data.length; i++, j++) {
            data[i] = message[j];
        }

        return data;
    }

    private boolean isValidPieceData(int pieceIndex, byte[] data) {
        return data.length == pieceSize && !PeerProcess.enumPieces.containsKey(pieceIndex);
    }

    private Piece createPiece(int pieceIndex, byte[] data) {
        return new Piece(pieceIndex, data);
    }

    private void sendHaveMessage(int pieceIndex) {
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
                PeerConnection completeFile = (PeerConnection) iterator.next();
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

    public byte[] updateBitField(byte[] field, int pieceIndex) {
        int position = pieceIndex - 1;
        field[(position / 8) + 5] = (byte) (field[(position / 8) + 5] | (1 << (7 - (position % 8))));
        return field;
    }

    private void updateBitfield(int pieceIndex) {
        try {
            synchronized (Bitfield.bitFieldBytes) {
                Bitfield.updateBitfield(pieceIndex);
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
